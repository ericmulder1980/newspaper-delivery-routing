package nl.ericmulder.krantenwijk.ui.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Building
import nl.ericmulder.krantenwijk.domain.model.BuildingContents
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.BuildingSnapshot
import nl.ericmulder.krantenwijk.domain.model.RouteSnapshot
import nl.ericmulder.krantenwijk.domain.model.SectionSnapshot
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

/** In-memory [RouteRepository] for ViewModel tests, mirroring the Room implementation's rules. */
class FakeRouteRepository : RouteRepository {
    val route = MutableStateFlow<Route?>(null)
    val segments = MutableStateFlow<List<Segment>>(emptyList())
    val addresses = MutableStateFlow<List<Address>>(emptyList())
    val buildings = MutableStateFlow<List<Building>>(emptyList())
    var failNextWrite = false
    private var nextId = 1L

    private fun maybeFail() {
        if (failNextWrite) {
            failNextWrite = false
            error("Simulated write failure")
        }
    }

    override fun observeRoute(): Flow<Route?> = route

    override suspend fun saveRoute(name: String, town: String?) {
        route.value = Route(name = name.trim(), town = town?.trim()?.ifEmpty { null }, createdAtMillis = 0, id = 1)
    }

    override fun observeSegments(): Flow<List<Segment>> = segments

    override fun observeSegmentContents(segmentId: Long): Flow<SegmentContents?> =
        combine(segments, addresses, buildings) { segs, addrs, blds ->
            segs.firstOrNull { it.id == segmentId }?.let { segment ->
                SegmentContents(
                    segment,
                    blds.filter { it.segmentId == segmentId },
                    inWalkingOrder(addrs.filter { it.segmentId == segmentId }, segment.direction),
                )
            }
        }

    override fun observeAllAddresses(): Flow<List<Address>> = addresses

    override fun observeAddressCounts(): Flow<Map<Long, Int>> =
        addresses.map { list -> list.filter { it.exists }.groupingBy { it.segmentId }.eachCount() }

    override fun observeStreetNames(): Flow<List<String>> =
        segments.map { list -> list.map { it.streetName }.distinct().sorted() }

    override suspend fun addSegment(streetName: String, side: Side, from: Int, to: Int, direction: Direction): Long {
        maybeFail()
        val id = nextId++
        segments.value += Segment(streetName.trim(), side, from, to, direction, segments.value.size, id)
        addresses.value += generateRange(from, to, side).map { Address(it, segmentId = id, id = nextId++) }
        return id
    }

    override suspend fun deleteSegment(segmentId: Long) {
        segments.value = segments.value.filter { it.id != segmentId }.mapIndexed { i, s -> s.copy(position = i) }
        addresses.value = addresses.value.filter { it.segmentId != segmentId }
    }

    override suspend fun reorderSegments(segmentIds: List<Long>) {
        require(segmentIds.sorted() == segments.value.map { it.id }.sorted()) { "Reorder must list every segment exactly once" }
        val byId = segments.value.associateBy { it.id }
        segments.value = segmentIds.mapIndexed { position, id -> byId.getValue(id).copy(position = position) }
    }

    override suspend fun setDirection(segmentId: Long, direction: Direction) {
        segments.value = segments.value.map { if (it.id == segmentId) it.copy(direction = direction) else it }
    }

    override suspend fun addAddress(segmentId: Long, houseNumber: Int, addition: String?): Long {
        maybeFail()
        val clean = addition?.trim()?.ifEmpty { null }
        if (addresses.value.any { it.segmentId == segmentId && it.houseNumber == houseNumber && it.addition == clean }) {
            throw DuplicateAddressException(houseNumber, clean)
        }
        val id = nextId++
        addresses.value += Address(houseNumber, clean, segmentId = segmentId, id = id)
        return id
    }

    override suspend fun deleteAddresses(addressIds: Collection<Long>) {
        addresses.value = addresses.value.filter { it.id !in addressIds }
    }

    override suspend fun setExists(addressIds: Collection<Long>, exists: Boolean) {
        addresses.value = addresses.value.map { if (it.id in addressIds) it.copy(exists = exists) else it }
    }

    override suspend fun setSticker(addressIds: Collection<Long>, sticker: Sticker) {
        addresses.value = addresses.value.map { if (it.id in addressIds) it.copy(sticker = sticker) else it }
    }

    override suspend fun updateAddress(address: Address) {
        addresses.value = addresses.value.map { if (it.id == address.id) address else it }
    }

    override suspend fun createBuilding(
        segmentId: Long,
        houseNumber: Int,
        addition: String?,
        suffixType: SuffixType,
        fromSuffix: String,
        toSuffix: String,
    ): Long {
        maybeFail()
        val cleanAddition = addition?.trim()?.ifEmpty { null }
        require(cleanAddition == null || suffixType == SuffixType.NUMBER) { "A building with an addition has numbered units" }
        val building = Building(segmentId, houseNumber, suffixType, separatorFor(suffixType), addition = cleanAddition)
        if (buildings.value.any { it.segmentId == segmentId && it.houseNumber == houseNumber && it.addition == cleanAddition }) {
            throw BuildingExistsException(building.label)
        }
        val additions = generateUnits(fromSuffix, toSuffix, suffixType).map(building::apartmentAddition)
        val conflicts = addresses.value
            .filter { it.segmentId == segmentId && it.houseNumber == houseNumber && it.buildingId == null && it.addition in additions }
            .map { "$houseNumber${it.addition}" }
        if (conflicts.isNotEmpty()) throw BuildingConflictException(conflicts)
        val id = nextId++
        buildings.value += building.copy(id = id)
        addresses.value = addresses.value.filterNot {
            it.segmentId == segmentId && it.houseNumber == houseNumber && it.addition == cleanAddition && it.buildingId == null
        } + additions.map { Address(houseNumber, it, segmentId = segmentId, buildingId = id, id = nextId++) }
        return id
    }

    override fun observeBuildingContents(buildingId: Long): Flow<BuildingContents?> =
        combine(segments, addresses, buildings) { segs, addrs, blds ->
            blds.firstOrNull { it.id == buildingId }?.let { building ->
                BuildingContents(
                    building,
                    segs.single { it.id == building.segmentId }.streetName,
                    addrs.filter { it.buildingId == buildingId }.sortedWith(AddressNumberOrder),
                )
            }
        }

    override suspend fun addApartment(buildingId: Long, suffix: String): Long {
        val building = buildings.value.single { it.id == buildingId }
        val clean = building.apartmentAddition(requireNotNull(normaliseSuffix(suffix, building.suffixType)))
        if (addresses.value.any { it.buildingId == buildingId && it.addition == clean }) {
            throw DuplicateAddressException(building.houseNumber, clean)
        }
        val id = nextId++
        addresses.value += Address(building.houseNumber, clean, segmentId = building.segmentId, buildingId = buildingId, id = id)
        return id
    }

    var failNextReplace = false

    override suspend fun snapshot(): RouteSnapshot? = route.value?.let { r ->
        RouteSnapshot(
            route = r,
            sections = segments.value.sortedBy { it.position }.map { seg ->
                SectionSnapshot(
                    segment = seg,
                    addresses = addresses.value.filter { it.segmentId == seg.id && it.buildingId == null },
                    buildings = buildings.value.filter { it.segmentId == seg.id }.map { b ->
                        BuildingSnapshot(b, addresses.value.filter { it.buildingId == b.id })
                    },
                )
            },
        )
    }

    override suspend fun replaceAll(snapshot: RouteSnapshot) {
        if (failNextReplace) {
            failNextReplace = false
            error("Simulated restore failure")
        }
        route.value = snapshot.route.copy(id = 1)
        segments.value = emptyList()
        addresses.value = emptyList()
        buildings.value = emptyList()
        snapshot.sections.forEachIndexed { position, section ->
            val segId = nextId++
            segments.value += section.segment.copy(id = segId, position = position)
            addresses.value += section.addresses.map { it.copy(id = nextId++, segmentId = segId, buildingId = null) }
            section.buildings.forEach { b ->
                val bId = nextId++
                buildings.value += b.building.copy(id = bId, segmentId = segId)
                addresses.value += b.apartments.map { it.copy(id = nextId++, segmentId = segId, buildingId = bId) }
            }
        }
    }

    override suspend fun removeBuilding(buildingId: Long) {
        val building = buildings.value.single { it.id == buildingId }
        buildings.value = buildings.value.filter { it.id != buildingId }
        addresses.value = addresses.value.filter { it.buildingId != buildingId } +
            Address(building.houseNumber, segmentId = building.segmentId, id = nextId++)
    }
}
