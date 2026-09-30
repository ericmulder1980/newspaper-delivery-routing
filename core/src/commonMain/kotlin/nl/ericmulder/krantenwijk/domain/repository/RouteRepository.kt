package nl.ericmulder.krantenwijk.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.SegmentContents
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker

/** Thrown when an address with the same number and addition already exists in the segment. */
class DuplicateAddressException(val houseNumber: Int, val addition: String?) :
    IllegalArgumentException("Address $houseNumber${addition.orEmpty()} already exists in this segment")

/** The route, its segments and addresses. Reads are reactive; every write is committed immediately (NFR-05). */
interface RouteRepository {
    fun observeRoute(): Flow<Route?>

    /** Creates the route, or renames the existing one (there is only one in v1). */
    suspend fun saveRoute(name: String, town: String?)

    /** Segments in walking order. */
    fun observeSegments(): Flow<List<Segment>>

    /** A segment with its buildings and addresses in walking order; null once the segment is deleted. */
    fun observeSegmentContents(segmentId: Long): Flow<SegmentContents?>

    /** Number of existing addresses per segment id, excluding "does not exist" (ADR-04). */
    fun observeAddressCounts(): Flow<Map<Long, Int>>

    /** Distinct street names used in the route, for auto-completion (ADR-07). */
    fun observeStreetNames(): Flow<List<String>>

    /**
     * Adds a segment at the end of the walking order with an address for every number in
     * [from]..[to] on [side] (DEC-012: from <= to). Returns the new segment's id.
     */
    suspend fun addSegment(streetName: String, side: Side, from: Int, to: Int, direction: Direction): Long

    suspend fun deleteSegment(segmentId: Long)

    /** Sets the walking order: [segmentIds] must contain every segment exactly once (ADR-05). */
    suspend fun reorderSegments(segmentIds: List<Long>)

    suspend fun setDirection(segmentId: Long, direction: Direction)

    /**
     * Adds a standalone address and returns its id.
     * @throws DuplicateAddressException if the same number and addition already exist in the segment.
     */
    suspend fun addAddress(segmentId: Long, houseNumber: Int, addition: String?): Long

    suspend fun deleteAddresses(addressIds: Collection<Long>)

    suspend fun setExists(addressIds: Collection<Long>, exists: Boolean)

    suspend fun setSticker(addressIds: Collection<Long>, sticker: Sticker)

    suspend fun updateAddress(address: Address)
}
