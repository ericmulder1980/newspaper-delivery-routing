package nl.ericmulder.krantenwijk.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        RouteEntity::class,
        SegmentEntity::class,
        BuildingEntity::class,
        AddressEntity::class,
        CompletedRoundEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@ConstructedBy(KrantenwijkDatabaseConstructor::class)
abstract class KrantenwijkDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao

    abstract fun segmentDao(): SegmentDao

    abstract fun buildingDao(): BuildingDao

    abstract fun addressDao(): AddressDao

    abstract fun backupDao(): BackupDao

    abstract fun completedRoundDao(): CompletedRoundDao

    companion object {
        const val FILE_NAME = "krantenwijk.db"
    }
}

// Room generates the actual implementations.
@Suppress("KotlinNoActualForExpect")
expect object KrantenwijkDatabaseConstructor : RoomDatabaseConstructor<KrantenwijkDatabase> {
    override fun initialize(): KrantenwijkDatabase
}

/**
 * Applies the settings shared by the app and tests: the bundled SQLite (same version on the phone
 * and in host tests) and IO dispatching. There is deliberately no destructive-migration fallback (DEC-002).
 */
fun RoomDatabase.Builder<KrantenwijkDatabase>.buildKrantenwijk(): KrantenwijkDatabase =
    setDriver(BundledSQLiteDriver())
        .addMigrations(*KrantenwijkMigrations)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
