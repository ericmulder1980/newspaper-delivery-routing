package nl.ericmulder.krantenwijk.data.db

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Explicit, non-destructive migrations (DEC-002). Each one's SQL is copied from the exported schema
 * of its target version (`core/schemas/…/<version>.json`) and checked by SchemaTest.
 */

/** v2 adds finished rounds (DEC-030); existing route data is untouched. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `completed_round` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`startedAtMillis` INTEGER NOT NULL, `finishedAtMillis` INTEGER NOT NULL, " +
                "`newspapers` INTEGER NOT NULL, `leaflets` INTEGER NOT NULL)",
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_completed_round_startedAtMillis` ON `completed_round` (`startedAtMillis`)",
        )
    }
}

/**
 * v3 gives a building its own addition, so 8A and 8B can both be buildings (DEC-031). Existing
 * buildings get "" (a plain number); the unique index moves from number to number + addition.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `building` ADD COLUMN `addition` TEXT NOT NULL DEFAULT ''")
        connection.execSQL("DROP INDEX IF EXISTS `index_building_segmentId_houseNumber`")
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_building_segmentId_houseNumber_addition` " +
                "ON `building` (`segmentId`, `houseNumber`, `addition`)",
        )
    }
}

val KrantenwijkMigrations: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
