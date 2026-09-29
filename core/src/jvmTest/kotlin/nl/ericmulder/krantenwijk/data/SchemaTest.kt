package nl.ericmulder.krantenwijk.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.io.path.Path

/**
 * The exported schema files must match the entities. Every future version adds a
 * `runMigrationsAndValidate` test here for its migration (DEC-002).
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
    fun `version 1 schema matches the entities`() {
        val helper = helper()
        helper.createDatabase(1).close()
        helper.runMigrationsAndValidate(1).close()
    }
}
