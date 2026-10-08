package nl.ericmulder.krantenwijk.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.execSQL
import kotlinx.coroutines.runBlocking
import nl.ericmulder.krantenwijk.data.db.MIGRATION_1_2
import nl.ericmulder.krantenwijk.data.db.MIGRATION_2_3
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.io.path.Path

/**
 * The exported schema files must match the entities. Every future version adds a
 * `runMigrationsAndValidate` test here for its migration (DEC-002). v1 → v2: DEC-030. v2 → v3: DEC-031.
 */
class SchemaTest {

    @TempDir
    lateinit var dir: File

    private fun helper() = MigrationTestHelper(
        schemaDirectoryPath = Path("schemas"),
        databasePath = Path(dir.resolve("migration-test.db").absolutePath),
        driver = BundledSQLiteDriver(),
        databaseClass = KrantenwijkDatabase::class,
    )

    @Test
    fun `version 3 schema matches the entities`() {
        val helper = helper()
        helper.createDatabase(3).close()
        helper.runMigrationsAndValidate(3).close()
    }

    @Test
    fun `migration 2 to 3 keeps buildings and allows 8A next to 8B (DEC-031)`() {
        val helper = helper()
        helper.createDatabase(2).use { db ->
            db.execSQL("INSERT INTO route (id, name, town, createdAtMillis) VALUES (1, 'Wijk 07', NULL, 0)")
            db.execSQL(
                "INSERT INTO segment (id, routeId, streetName, side, rangeFrom, rangeTo, direction, position) " +
                    "VALUES (1, 1, 'Kerkstraat', 'EVEN', 2, 24, 'ASCENDING', 0)",
            )
            db.execSQL("INSERT INTO building (id, segmentId, houseNumber, name, suffixType, separator) VALUES (1, 1, 12, NULL, 'LETTER', '')")
        }
        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).use { db ->
            db.prepare("SELECT houseNumber, addition FROM building").use {
                assertTrue(it.step())
                assertEquals(12L, it.getLong(0))
                assertEquals("", it.getText(1))
            }
            db.execSQL("INSERT INTO building (segmentId, houseNumber, name, suffixType, separator, addition) VALUES (1, 8, NULL, 'NUMBER', '-', 'A')")
            db.execSQL("INSERT INTO building (segmentId, houseNumber, name, suffixType, separator, addition) VALUES (1, 8, NULL, 'NUMBER', '-', 'B')")
            db.prepare("SELECT COUNT(*) FROM building").use { assertTrue(it.step()); assertEquals(3L, it.getLong(0)) }
            val duplicate = runCatching {
                db.execSQL("INSERT INTO building (segmentId, houseNumber, name, suffixType, separator, addition) VALUES (1, 8, NULL, 'NUMBER', '-', 'A')")
            }
            assertTrue(duplicate.isFailure, "a second 8A must violate the unique index")
        }
    }

    @Test
    fun `migration 1 to 2 keeps the route and adds finished rounds (DEC-030)`() {
        val helper = helper()
        helper.createDatabase(1).use { db ->
            db.execSQL("INSERT INTO route (id, name, town, createdAtMillis) VALUES (1, 'Wijk 07', NULL, 0)")
            db.execSQL(
                "INSERT INTO segment (id, routeId, streetName, side, rangeFrom, rangeTo, direction, position) " +
                    "VALUES (1, 1, 'Kerkstraat', 'EVEN', 2, 24, 'ASCENDING', 0)",
            )
        }
        helper.runMigrationsAndValidate(2, listOf(MIGRATION_1_2)).use { db ->
            db.prepare("SELECT name FROM route").use { assertTrue(it.step()); assertEquals("Wijk 07", it.getText(0)) }
            db.prepare("SELECT streetName FROM segment").use { assertTrue(it.step()); assertEquals("Kerkstraat", it.getText(0)) }
            db.execSQL("INSERT INTO completed_round (startedAtMillis, finishedAtMillis, newspapers, leaflets) VALUES (0, 60000, 57, 41)")
            db.prepare("SELECT COUNT(*) FROM completed_round").use { assertTrue(it.step()); assertEquals(1L, it.getLong(0)) }
        }
    }

    @Test
    fun `the app opens a version 1 database without losing data`() {
        helper().run {
            createDatabase(1).use { it.execSQL("INSERT INTO route (id, name, town, createdAtMillis) VALUES (1, 'Wijk 07', NULL, 0)") }
        }
        // Same file name as the helper uses; the builder must find the migration on its own.
        val db = fileDatabase(dir.resolve("migration-test.db").absolutePath)
        try {
            runBlocking { assertEquals("Wijk 07", db.routeDao().get()?.name) }
        } finally {
            db.close()
        }
    }
}
