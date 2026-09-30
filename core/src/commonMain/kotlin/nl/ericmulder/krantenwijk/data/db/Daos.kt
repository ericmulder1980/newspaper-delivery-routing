package nl.ericmulder.krantenwijk.data.db

import androidx.room.Dao
import androidx.room.Insert
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

@Dao
interface BuildingDao {
    @Query("SELECT * FROM building WHERE segmentId = :segmentId ORDER BY houseNumber")
    fun observeForSegment(segmentId: Long): Flow<List<BuildingEntity>>

    @Insert
    suspend fun insert(building: BuildingEntity): Long

    @Update
    suspend fun update(building: BuildingEntity)

    @Query("DELETE FROM building WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM address WHERE segmentId = :segmentId")
    fun observeForSegment(segmentId: Long): Flow<List<AddressEntity>>

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
