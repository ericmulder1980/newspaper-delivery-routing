package nl.ericmulder.krantenwijk.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.BuildingContents
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.SegmentContents
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType

/** Thrown when an address with the same number and addition already exists in the segment. */
class DuplicateAddressException(val houseNumber: Int, val addition: String?) :
    IllegalArgumentException("Address $houseNumber${addition.orEmpty()} already exists in this segment")

/** Thrown when an apartment of a new building would collide with an existing standalone address. */
class BuildingConflictException(val labels: List<String>) :
    IllegalArgumentException("Standalone addresses already use these numbers: ${labels.joinToString()}")

/** The route, its segments and addresses. Reads are reactive; every write is committed immediately (NFR-05). */
interface RouteRepository {
    fun observeRoute(): Flow<Route?>

    /** Creates the route, or renames the existing one (there is only one in v1). */
    suspend fun saveRoute(name: String, town: String?)

    /** Segments in walking order. */
    fun observeSegments(): Flow<List<Segment>>

    /** A segment with its buildings and addresses in walking order; null once the segment is deleted. */
    fun observeSegmentContents(segmentId: Long): Flow<SegmentContents?>

    /** Every address in the route (houses and apartments), for route-wide totals (RND-02). */
    fun observeAllAddresses(): Flow<List<Address>>

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

    /**
     * Turns [houseNumber] in the segment into an apartment building with units [fromSuffix]..[toSuffix]
     * (BLD-01). The plain standalone address with that number, if any, is replaced. Returns the
     * building id.
     * @throws BuildingConflictException if a standalone address already has one of the unit labels.
     * @throws IllegalArgumentException if the unit range is invalid.
     */
    suspend fun createBuilding(segmentId: Long, houseNumber: Int, suffixType: SuffixType, fromSuffix: String, toSuffix: String): Long

    /** A building with its street name and apartments; null once the building is removed. */
    fun observeBuildingContents(buildingId: Long): Flow<BuildingContents?>

    /**
     * Adds one apartment to a building (BLD-05) and returns its id.
     * @throws IllegalArgumentException if [suffix] doesn't fit the building's type (e.g. "AB" or "0").
     * @throws DuplicateAddressException if the apartment already exists.
     */
    suspend fun addApartment(buildingId: Long, suffix: String): Long

    /** Removes a building and its apartments and puts back a plain address with its number ("no longer a building"). */
    suspend fun removeBuilding(buildingId: Long)
}
