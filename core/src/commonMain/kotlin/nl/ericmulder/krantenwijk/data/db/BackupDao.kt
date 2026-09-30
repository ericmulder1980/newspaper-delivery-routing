package nl.ericmulder.krantenwijk.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import nl.ericmulder.krantenwijk.domain.model.BuildingSnapshot
import nl.ericmulder.krantenwijk.domain.model.RouteSnapshot
import nl.ericmulder.krantenwijk.domain.model.SectionSnapshot

/** Reads and replaces the whole route for backup and restore (DATA-02). */
@Dao
abstract class BackupDao {
    @Query("SELECT * FROM route ORDER BY id LIMIT 1")
    protected abstract suspend fun route(): RouteEntity?

    @Query("SELECT * FROM segment ORDER BY position")
    protected abstract suspend fun segments(): List<SegmentEntity>

    @Query("SELECT * FROM building")
    protected abstract suspend fun buildings(): List<BuildingEntity>

    @Query("SELECT * FROM address")
    protected abstract suspend fun addresses(): List<AddressEntity>

    /** Deletes everything: segments, buildings and addresses cascade from the route. */
    @Query("DELETE FROM route")
    protected abstract suspend fun deleteRoutes()

    @Insert
    protected abstract suspend fun insertRoute(route: RouteEntity): Long

    @Insert
    protected abstract suspend fun insertSegment(segment: SegmentEntity): Long

    @Insert
    protected abstract suspend fun insertBuilding(building: BuildingEntity): Long

    @Insert
    protected abstract suspend fun insertAddresses(addresses: List<AddressEntity>)

    /** The route in walking order with everything in it; one transaction for a consistent picture. */
    @Transaction
    open suspend fun snapshot(): RouteSnapshot? {
        val route = route() ?: return null
        val buildingsBySegment = buildings().groupBy { it.segmentId }
        val addresses = addresses()
        val standaloneBySegment = addresses.filter { it.buildingId == null }.groupBy { it.segmentId }
        val apartmentsByBuilding = addresses.filter { it.buildingId != null }.groupBy { it.buildingId }
        return RouteSnapshot(
            route = route.toDomain(),
            sections = segments().map { segment ->
                SectionSnapshot(
                    segment = segment.toDomain(),
                    addresses = standaloneBySegment[segment.id].orEmpty().map { it.toDomain() }.sortedBy { it.houseNumber },
                    buildings = buildingsBySegment[segment.id].orEmpty().sortedBy { it.houseNumber }.map { b ->
                        BuildingSnapshot(b.toDomain(), apartmentsByBuilding[b.id].orEmpty().map { it.toDomain() })
                    },
                )
            },
        )
    }

    /** Replaces the route with [snapshot]; if anything fails, the transaction rolls back and nothing changes. */
    @Transaction
    open suspend fun replaceAll(snapshot: RouteSnapshot) {
        deleteRoutes()
        val routeId = insertRoute(
            RouteEntity(name = snapshot.route.name, town = snapshot.route.town, createdAtMillis = snapshot.route.createdAtMillis),
        )
        snapshot.sections.forEachIndexed { position, section ->
            val s = section.segment
            val segmentId = insertSegment(
                SegmentEntity(
                    routeId = routeId,
                    streetName = s.streetName,
                    side = s.side,
                    rangeFrom = s.rangeFrom,
                    rangeTo = s.rangeTo,
                    direction = s.direction,
                    position = position,
                ),
            )
            insertAddresses(section.addresses.map { it.copy(id = 0, segmentId = segmentId, buildingId = null).toEntity() })
            section.buildings.forEach { b ->
                val buildingId = insertBuilding(
                    BuildingEntity(
                        segmentId = segmentId,
                        houseNumber = b.building.houseNumber,
                        name = b.building.name,
                        suffixType = b.building.suffixType,
                        separator = b.building.separator,
                    ),
                )
                insertAddresses(
                    b.apartments.map {
                        it.copy(id = 0, segmentId = segmentId, buildingId = buildingId, houseNumber = b.building.houseNumber).toEntity()
                    },
                )
            }
        }
    }
}
