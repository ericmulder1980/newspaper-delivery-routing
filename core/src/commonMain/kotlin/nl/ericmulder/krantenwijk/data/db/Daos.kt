package nl.ericmulder.krantenwijk.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Sticker

@Dao
interface RouteDao {
    @Query("SELECT * FROM route ORDER BY id LIMIT 1")
    fun observe(): Flow<RouteEntity?>

    @Query("SELECT * FROM route ORDER BY id LIMIT 1")
    suspend fun get(): RouteEntity?

    @Insert
    suspend fun insert(route: RouteEntity): Long

    @Update
    suspend fun update(route: RouteEntity)
}

@Dao
abstract class SegmentDao {
    @Query("SELECT * FROM segment ORDER BY position")
    abstract fun observeAll(): Flow<List<SegmentEntity>>

    @Query("SELECT * FROM segment WHERE id = :id")
    abstract fun observe(id: Long): Flow<SegmentEntity?>

    @Query("SELECT id FROM segment ORDER BY position")
    abstract suspend fun idsInOrder(): List<Long>

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM segment")
    abstract suspend fun nextPosition(): Int

    @Query("SELECT DISTINCT streetName FROM segment ORDER BY streetName COLLATE NOCASE")
    abstract fun observeStreetNames(): Flow<List<String>>

    @Insert
    abstract suspend fun insert(segment: SegmentEntity): Long

    @Insert
    protected abstract suspend fun insertAddresses(addresses: List<AddressEntity>)

    @Query("UPDATE segment SET direction = :direction WHERE id = :id")
    abstract suspend fun setDirection(id: Long, direction: Direction)

    @Query("UPDATE segment SET position = :position WHERE id = :id")
    protected abstract suspend fun setPosition(id: Long, position: Int)

    @Query("DELETE FROM segment WHERE id = :id")
    protected abstract suspend fun delete(id: Long)

    /** Inserts [segment] and its generated addresses in one transaction; returns the segment id. */
    @Transaction
    open suspend fun insertWithAddresses(segment: SegmentEntity, addresses: (segmentId: Long) -> List<AddressEntity>): Long {
        val id = insert(segment)
        insertAddresses(addresses(id))
        return id
    }

    /** Assigns positions 0..n-1 in the given order. */
    @Transaction
    open suspend fun reorder(ids: List<Long>) {
        ids.forEachIndexed { index, id -> setPosition(id, index) }
    }

    /** Deletes a segment (addresses and buildings cascade) and closes the gap in positions. */
    @Transaction
    open suspend fun deleteAndCompact(id: Long) {
        delete(id)
        reorder(idsInOrder())
    }
}

/** Buildings and their apartments; creating and removing a building each happen in one transaction. */
@Dao
abstract class BuildingDao {
    @Query("SELECT * FROM building WHERE segmentId = :segmentId ORDER BY houseNumber, addition")
    abstract fun observeForSegment(segmentId: Long): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM building WHERE id = :id")
    abstract suspend fun get(id: Long): BuildingEntity?

    @Query("SELECT * FROM building WHERE id = :id")
    abstract fun observe(id: Long): Flow<BuildingEntity?>

    @Insert
    abstract suspend fun insert(building: BuildingEntity): Long

    @Update
    abstract suspend fun update(building: BuildingEntity)

    @Query("DELETE FROM building WHERE id = :id")
    abstract suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM building WHERE segmentId = :segmentId AND houseNumber = :houseNumber AND addition = :addition")
    protected abstract suspend fun countWithLabel(segmentId: Long, houseNumber: Int, addition: String): Int

    @Query("SELECT * FROM address WHERE segmentId = :segmentId AND houseNumber = :houseNumber AND buildingId IS NULL")
    protected abstract suspend fun standaloneWithNumber(segmentId: Long, houseNumber: Int): List<AddressEntity>

    @Query("DELETE FROM address WHERE segmentId = :segmentId AND houseNumber = :houseNumber AND addition = :addition AND buildingId IS NULL")
    protected abstract suspend fun deleteStandalone(segmentId: Long, houseNumber: Int, addition: String)

    @Insert
    protected abstract suspend fun insertAddresses(addresses: List<AddressEntity>)

    /**
     * Replaces the standalone address with [building]'s number and addition (12, or 8A: DEC-031)
     * with a building and one apartment per stored addition in [apartmentAdditions].
     * Returns [CreateResult.Created], or what's in the way without writing anything.
     */
    @Transaction
    open suspend fun createWithUnits(building: BuildingEntity, apartmentAdditions: List<String>): CreateResult {
        if (countWithLabel(building.segmentId, building.houseNumber, building.addition) > 0) return CreateResult.AlreadyExists
        val taken = standaloneWithNumber(building.segmentId, building.houseNumber).map { it.addition }.toSet()
        val conflicts = apartmentAdditions.filter { it in taken }
        if (conflicts.isNotEmpty()) return CreateResult.Conflicts(conflicts)
        deleteStandalone(building.segmentId, building.houseNumber, building.addition)
        val id = insert(building)
        insertAddresses(apartmentAdditions.map { newAddress(building.segmentId, id, building.houseNumber, it) })
        return CreateResult.Created(id)
    }

    /** Deletes the building (apartments cascade) and restores a standalone address with its label (12 or 8A). */
    @Transaction
    open suspend fun removeAndRestore(buildingId: Long) {
        val building = get(buildingId) ?: return
        delete(buildingId)
        if (standaloneWithNumber(building.segmentId, building.houseNumber).none { it.addition == building.addition }) {
            insertAddresses(listOf(newAddress(building.segmentId, null, building.houseNumber, building.addition)))
        }
    }

    private fun newAddress(segmentId: Long, buildingId: Long?, houseNumber: Int, addition: String) = AddressEntity(
        segmentId = segmentId,
        buildingId = buildingId,
        houseNumber = houseNumber,
        addition = addition,
        exists = true,
        sticker = Sticker.NONE,
        exceptionNoNewspaper = false,
        exceptionNoLeaflets = false,
        note = null,
    )

    sealed interface CreateResult {
        data class Created(val id: Long) : CreateResult

        /** A building with this number and addition is already there. */
        data object AlreadyExists : CreateResult

        /** Standalone addresses already use these stored additions. */
        data class Conflicts(val additions: List<String>) : CreateResult
    }
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM address WHERE segmentId = :segmentId")
    fun observeForSegment(segmentId: Long): Flow<List<AddressEntity>>

    @Query("SELECT * FROM address")
    fun observeAll(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM address WHERE buildingId = :buildingId")
    fun observeForBuilding(buildingId: Long): Flow<List<AddressEntity>>

    @Query("SELECT * FROM address WHERE id = :id")
    suspend fun get(id: Long): AddressEntity?

    @Query("SELECT COUNT(*) FROM address WHERE segmentId = :segmentId AND houseNumber = :houseNumber AND addition = :addition")
    suspend fun count(segmentId: Long, houseNumber: Int, addition: String): Int

    @Query("SELECT segmentId, COUNT(*) AS count FROM address WHERE `exists` = 1 GROUP BY segmentId")
    fun observeExistingCounts(): Flow<List<SegmentCount>>

    @Insert
    suspend fun insert(address: AddressEntity): Long

    @Insert
    suspend fun insertAll(addresses: List<AddressEntity>)

    @Update
    suspend fun update(address: AddressEntity)

    @Query("DELETE FROM address WHERE id IN (:ids)")
    suspend fun delete(ids: Collection<Long>)

    @Query("UPDATE address SET `exists` = :exists WHERE id IN (:ids)")
    suspend fun setExists(ids: Collection<Long>, exists: Boolean)

    @Query("UPDATE address SET sticker = :sticker WHERE id IN (:ids)")
    suspend fun setSticker(ids: Collection<Long>, sticker: Sticker)
}

/** Result row for [AddressDao.observeExistingCounts]. */
data class SegmentCount(val segmentId: Long, val count: Int)

@Dao
interface CompletedRoundDao {
    @Query("SELECT * FROM completed_round")
    fun observeAll(): Flow<List<CompletedRoundEntity>>

    @Query("SELECT * FROM completed_round WHERE id = :id")
    fun observe(id: Long): Flow<CompletedRoundEntity?>

    @Query("SELECT id FROM completed_round WHERE startedAtMillis = :startedAtMillis")
    suspend fun idStartedAt(startedAtMillis: Long): Long?

    /** Returns -1 when a round with the same start time already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(round: CompletedRoundEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rounds: List<CompletedRoundEntity>)

    @Query("DELETE FROM completed_round WHERE id IN (:ids)")
    suspend fun delete(ids: Collection<Long>)
}
