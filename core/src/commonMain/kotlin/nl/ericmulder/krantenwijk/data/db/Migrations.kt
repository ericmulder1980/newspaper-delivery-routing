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

val KrantenwijkMigrations: Array<Migration> = arrayOf(MIGRATION_1_2)
