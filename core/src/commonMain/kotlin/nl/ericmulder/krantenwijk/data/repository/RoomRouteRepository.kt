package nl.ericmulder.krantenwijk.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.ericmulder.krantenwijk.data.db.BuildingDao
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.db.RouteEntity
import nl.ericmulder.krantenwijk.data.db.SegmentEntity
import nl.ericmulder.krantenwijk.data.db.toDomain
import nl.ericmulder.krantenwijk.data.db.toEntity
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Building
import nl.ericmulder.krantenwijk.domain.model.BuildingContents
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.RouteSnapshot
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.SegmentContents
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.domain.model.apartmentAddition
import nl.ericmulder.krantenwijk.domain.model.label
import nl.ericmulder.krantenwijk.domain.repository.BuildingConflictException
import nl.ericmulder.krantenwijk.domain.repository.BuildingExistsException
import nl.ericmulder.krantenwijk.domain.repository.DuplicateAddressException
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.rules.AddressNumberOrder
import nl.ericmulder.krantenwijk.domain.rules.generateRange
import nl.ericmulder.krantenwijk.domain.rules.generateUnits
import nl.ericmulder.krantenwijk.domain.rules.separatorFor
import nl.ericmulder.krantenwijk.domain.rules.inWalkingOrder
import nl.ericmulder.krantenwijk.domain.rules.normaliseSuffix

class RoomRouteRepository(
    private val db: KrantenwijkDatabase,
    private val clock: () -> Long,
) : RouteRepository {

    private val routeDao = db.routeDao()
    private val segmentDao = db.segmentDao()
    private val buildingDao = db.buildingDao()
    private val addressDao = db.addressDao()

    override fun observeRoute(): Flow<Route?> = routeDao.observe().map { it?.toDomain() }

    override suspend fun saveRoute(name: String, town: String?) {
        require(name.isNotBlank()) { "Route name must not be blank" }
        val cleanTown = town?.trim()?.ifEmpty { null }
        val existing = routeDao.get()
        if (existing == null) {
            routeDao.insert(RouteEntity(name = name.trim(), town = cleanTown, createdAtMillis = clock()))
        } else {
            routeDao.update(existing.copy(name = name.trim(), town = cleanTown))
        }
    }

    override fun observeSegments(): Flow<List<Segment>> =
        segmentDao.observeAll().map { segments -> segments.map { it.toDomain() } }

    override fun observeSegmentContents(segmentId: Long): Flow<SegmentContents?> = combine(
        segmentDao.observe(segmentId),
        buildingDao.observeForSegment(segmentId),
        addressDao.observeForSegment(segmentId),
    ) { segment, buildings, addresses ->
        segment?.toDomain()?.let {
            SegmentContents(
                segment = it,
                buildings = buildings.map { b -> b.toDomain() },
                addresses = inWalkingOrder(addresses.map { a -> a.toDomain() }, it.direction),
            )
        }
    }

    override fun observeAllAddresses(): Flow<List<Address>> =
        addressDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeAddressCounts(): Flow<Map<Long, Int>> =
        addressDao.observeExistingCounts().map { rows -> rows.associate { it.segmentId to it.count } }

    override fun observeStreetNames(): Flow<List<String>> = segmentDao.observeStreetNames()

    override suspend fun addSegment(streetName: String, side: Side, from: Int, to: Int, direction: Direction): Long {
        require(streetName.isNotBlank()) { "Street name must not be blank" }
        val route = checkNotNull(routeDao.get()) { "Create the route before adding segments" }
        val numbers = generateRange(from, to, side)
        val segment = SegmentEntity(
            routeId = route.id,
            streetName = streetName.trim(),
            side = side,
            rangeFrom = from,
            rangeTo = to,
            direction = direction,
            position = segmentDao.nextPosition(),
        )
        return segmentDao.insertWithAddresses(segment) { segmentId ->
            numbers.map { Address(houseNumber = it, segmentId = segmentId).toEntity() }
        }
    }

    override suspend fun deleteSegment(segmentId: Long) = segmentDao.deleteAndCompact(segmentId)

    override suspend fun reorderSegments(segmentIds: List<Long>) {
        val current = segmentDao.idsInOrder()
        require(segmentIds.size == current.size && segmentIds.toSet() == current.toSet()) {
            "Reorder must list every segment exactly once"
        }
        segmentDao.reorder(segmentIds)
    }

    override suspend fun setDirection(segmentId: Long, direction: Direction) =
        segmentDao.setDirection(segmentId, direction)

    override suspend fun addAddress(segmentId: Long, houseNumber: Int, addition: String?): Long {
        val cleanAddition = addition?.trim()?.ifEmpty { null }
        if (addressDao.count(segmentId, houseNumber, cleanAddition.orEmpty()) > 0) {
            throw DuplicateAddressException(houseNumber, cleanAddition)
        }
        return addressDao.insert(Address(houseNumber = houseNumber, addition = cleanAddition, segmentId = segmentId).toEntity())
    }

    override suspend fun deleteAddresses(addressIds: Collection<Long>) = addressDao.delete(addressIds)

    override suspend fun setExists(addressIds: Collection<Long>, exists: Boolean) =
        addressDao.setExists(addressIds, exists)

    override suspend fun setSticker(addressIds: Collection<Long>, sticker: Sticker) =
        addressDao.setSticker(addressIds, sticker)

    override suspend fun updateAddress(address: Address) {
        require(address.id != 0L) { "Address has not been stored yet" }
        addressDao.update(address.toEntity())
    }

    override suspend fun createBuilding(
        segmentId: Long,
        houseNumber: Int,
        addition: String?,
        suffixType: SuffixType,
        fromSuffix: String,
        toSuffix: String,
    ): Long {
        val cleanAddition = addition?.trim()?.ifEmpty { null }
        require(cleanAddition == null || suffixType == SuffixType.NUMBER) { "A building with an addition has numbered units" }
        val building = Building(
            segmentId = segmentId,
            houseNumber = houseNumber,
            suffixType = suffixType,
            separator = separatorFor(suffixType),
            addition = cleanAddition,
        )
        val additions = generateUnits(fromSuffix, toSuffix, suffixType).map(building::apartmentAddition)
        return when (val result = buildingDao.createWithUnits(building.toEntity(), additions)) {
            is BuildingDao.CreateResult.Created -> result.id
            BuildingDao.CreateResult.AlreadyExists -> throw BuildingExistsException(building.label)
            is BuildingDao.CreateResult.Conflicts -> throw BuildingConflictException(result.additions.map { "$houseNumber$it" })
        }
    }

    override suspend fun removeBuilding(buildingId: Long) = buildingDao.removeAndRestore(buildingId)

    override suspend fun snapshot(): RouteSnapshot? = db.backupDao().snapshot()

    override suspend fun replaceAll(snapshot: RouteSnapshot) = db.backupDao().replaceAll(snapshot)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeBuildingContents(buildingId: Long): Flow<BuildingContents?> =
        buildingDao.observe(buildingId).flatMapLatest { building ->
            if (building == null) {
                flowOf(null)
            } else {
                combine(segmentDao.observe(building.segmentId), addressDao.observeForBuilding(buildingId)) { segment, apartments ->
                    segment?.let {
                        BuildingContents(
                            building = building.toDomain(),
                            streetName = it.streetName,
                            apartments = apartments.map { a -> a.toDomain() }.sortedWith(AddressNumberOrder),
                        )
                    }
                }
            }
        }

    override suspend fun addApartment(buildingId: Long, suffix: String): Long {
        val building = requireNotNull(buildingDao.get(buildingId)) { "Building $buildingId does not exist" }
        val clean = requireNotNull(normaliseSuffix(suffix, building.suffixType)) {
            "\"$suffix\" is not a valid ${building.suffixType} suffix"
        }
        val addition = building.toDomain().apartmentAddition(clean)
        if (addressDao.count(building.segmentId, building.houseNumber, addition) > 0) {
            throw DuplicateAddressException(building.houseNumber, addition)
        }
        return addressDao.insert(
            Address(houseNumber = building.houseNumber, addition = addition, segmentId = building.segmentId, buildingId = buildingId)
                .toEntity(),
        )
    }
}
