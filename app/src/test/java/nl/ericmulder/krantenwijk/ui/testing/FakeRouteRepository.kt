package nl.ericmulder.krantenwijk.ui.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.SegmentContents
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository

/** In-memory [RouteRepository] for ViewModel tests; only what screens use so far is implemented. */
class FakeRouteRepository : RouteRepository {
    val route = MutableStateFlow<Route?>(null)
    val segments = MutableStateFlow<List<Segment>>(emptyList())
    var failNextWrite = false
    private var nextId = 1L

    override fun observeRoute(): Flow<Route?> = route

    override suspend fun saveRoute(name: String, town: String?) {
        route.value = Route(name = name.trim(), town = town?.trim()?.ifEmpty { null }, createdAtMillis = 0, id = 1)
    }

    override fun observeSegments(): Flow<List<Segment>> = segments

    override fun observeSegmentContents(segmentId: Long): Flow<SegmentContents?> = throw NotImplementedError()

    override fun observeStreetNames(): Flow<List<String>> =
        segments.map { list -> list.map { it.streetName }.distinct().sorted() }

    override suspend fun addSegment(streetName: String, side: Side, from: Int, to: Int, direction: Direction): Long {
        if (failNextWrite) {
            failNextWrite = false
            error("Simulated write failure")
        }
        val id = nextId++
        segments.value += Segment(streetName.trim(), side, from, to, direction, segments.value.size, id)
        return id
    }

    override suspend fun deleteSegment(segmentId: Long) = throw NotImplementedError()

    override suspend fun reorderSegments(segmentIds: List<Long>) = throw NotImplementedError()

    override suspend fun setDirection(segmentId: Long, direction: Direction) = throw NotImplementedError()

    override suspend fun addAddress(segmentId: Long, houseNumber: Int, addition: String?): Long = throw NotImplementedError()

    override suspend fun deleteAddresses(addressIds: Collection<Long>) = throw NotImplementedError()

    override suspend fun setExists(addressIds: Collection<Long>, exists: Boolean) = throw NotImplementedError()

    override suspend fun setSticker(addressIds: Collection<Long>, sticker: Sticker) = throw NotImplementedError()

    override suspend fun updateAddress(address: Address) = throw NotImplementedError()
}
