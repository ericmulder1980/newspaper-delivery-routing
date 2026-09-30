package nl.ericmulder.krantenwijk.ui.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.SegmentContents
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.repository.DuplicateAddressException
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.rules.generateRange
import nl.ericmulder.krantenwijk.domain.rules.inWalkingOrder

/** In-memory [RouteRepository] for ViewModel tests, mirroring the Room implementation's rules. */
class FakeRouteRepository : RouteRepository {
    val route = MutableStateFlow<Route?>(null)
    val segments = MutableStateFlow<List<Segment>>(emptyList())
    val addresses = MutableStateFlow<List<Address>>(emptyList())
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
        combine(segments, addresses) { segs, addrs ->
            segs.firstOrNull { it.id == segmentId }?.let { segment ->
                SegmentContents(segment, emptyList(), inWalkingOrder(addrs.filter { it.segmentId == segmentId }, segment.direction))
            }
        }

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

    override suspend fun reorderSegments(segmentIds: List<Long>) = throw NotImplementedError()

    override suspend fun setDirection(segmentId: Long, direction: Direction) = throw NotImplementedError()

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
}
