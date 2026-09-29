package nl.ericmulder.krantenwijk.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.db.RouteEntity
import nl.ericmulder.krantenwijk.data.db.SegmentEntity
import nl.ericmulder.krantenwijk.data.db.toDomain
import nl.ericmulder.krantenwijk.data.db.toEntity
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.SegmentContents
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.rules.generateRange
import nl.ericmulder.krantenwijk.domain.rules.inWalkingOrder

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

    override suspend fun addAddress(segmentId: Long, houseNumber: Int, addition: String?): Long =
        addressDao.insert(
            Address(houseNumber = houseNumber, addition = addition?.trim()?.ifEmpty { null }, segmentId = segmentId).toEntity(),
        )

    override suspend fun deleteAddresses(addressIds: Collection<Long>) = addressDao.delete(addressIds)

    override suspend fun setExists(addressIds: Collection<Long>, exists: Boolean) =
        addressDao.setExists(addressIds, exists)

    override suspend fun setSticker(addressIds: Collection<Long>, sticker: Sticker) =
        addressDao.setSticker(addressIds, sticker)

    override suspend fun updateAddress(address: Address) {
        require(address.id != 0L) { "Address has not been stored yet" }
        addressDao.update(address.toEntity())
    }
}
