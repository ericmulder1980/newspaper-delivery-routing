package nl.ericmulder.krantenwijk.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType

// Schema v1 (plan §5, without Round/RoundItem: DEC-014); v2 adds completed_round (DEC-030);
// v3 adds building.addition (DEC-031).
// Changes need a new version, an explicit migration and a migration test (DEC-002).

@Entity(tableName = "route")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val town: String?,
    val createdAtMillis: Long,
)

@Entity(
    tableName = "segment",
    foreignKeys = [ForeignKey(RouteEntity::class, ["id"], ["routeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("routeId")],
)
data class SegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: Long,
    val streetName: String,
    val side: Side,
    val rangeFrom: Int,
    val rangeTo: Int,
    val direction: Direction,
    val position: Int,
)

@Entity(
    tableName = "building",
    foreignKeys = [ForeignKey(SegmentEntity::class, ["id"], ["segmentId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["segmentId", "houseNumber", "addition"], unique = true)],
)
data class BuildingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val segmentId: Long,
    val houseNumber: Int,
    val name: String?,
    val suffixType: SuffixType,
    val separator: String,
    /** "" for a plain number (as in [AddressEntity.addition]), e.g. "A" for building 8A (DEC-031). */
    @ColumnInfo(defaultValue = "") val addition: String = "",
)

/**
 * [addition] is "" rather than NULL when there is none, so the unique index also prevents duplicate
 * plain numbers (SQLite treats NULLs as distinct in unique indexes).
 */
@Entity(
    tableName = "address",
    foreignKeys = [
        ForeignKey(SegmentEntity::class, ["id"], ["segmentId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(BuildingEntity::class, ["id"], ["buildingId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["segmentId", "houseNumber", "addition"], unique = true), Index("buildingId")],
)
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val segmentId: Long,
    val buildingId: Long?,
    val houseNumber: Int,
    val addition: String,
    val exists: Boolean,
    val sticker: Sticker,
    val exceptionNoNewspaper: Boolean,
    val exceptionNoLeaflets: Boolean,
    val note: String?,
)

/**
 * A finished round (DEC-030). The unique start time makes finishing idempotent: a round finished
 * twice (crash between saving it and clearing the active round) is stored once.
 */
@Entity(tableName = "completed_round", indices = [Index(value = ["startedAtMillis"], unique = true)])
data class CompletedRoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
    val newspapers: Int,
    val leaflets: Int,
)
