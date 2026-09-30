package nl.ericmulder.krantenwijk.ui.backup

import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.data.BackupStorage
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeSettingsRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class BackupViewModelTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private class MemoryStorage : BackupStorage {
        val files = mutableMapOf<String, String>()
        var failWrites = false

        override suspend fun write(uri: String, text: String) {
            if (failWrites) error("disk full")
            files[uri] = text
        }

        override suspend fun read(uri: String): String = files[uri] ?: error("no such file")
    }

    private val repo = FakeRouteRepository()
    private val settings = FakeSettingsRepository()
    private val storage = MemoryStorage()
    private val vm by lazy { BackupViewModel(repo, settings, storage, clock = { 1_000 }) }

    private suspend fun route() {
        repo.saveRoute("Wijk 07", "Houten")
        val id = repo.addSegment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING)
        repo.addSegment("Molenweg", Side.ALL, 1, 10, Direction.DESCENDING)
        repo.setSticker(listOf(repo.addresses.value.first { it.segmentId == id }.id), Sticker.NEE_JA)
        repo.createBuilding(id, 12, SuffixType.LETTER, "A", "D")
    }

    @Test
    fun `export writes the route and reports what was saved`() = runTest {
        route()
        vm.export("content://backup.json")
        assertEquals(BackupStatus.Exported(sections = 2, addresses = 11 + 4 + 10), vm.status.value)
        assertTrue(storage.files.getValue("content://backup.json").contains("\"Wijk 07\""))
    }

    @Test
    fun `export without a route or when writing fails`() = runTest {
        vm.export("content://a.json")
        assertEquals(BackupStatus.Failed(BackupError.NO_ROUTE), vm.status.value)
        route()
        storage.failWrites = true
        vm.export("content://a.json")
        assertEquals(BackupStatus.Failed(BackupError.WRITE_FAILED), vm.status.value)
    }

    @Test
    fun `restore asks for confirmation, then replaces the route and completes setup`() = runTest {
        route()
        vm.export("content://b.json")
        repo.saveRoute("Something else", null)
        repo.deleteSegment(repo.segments.value.first().id)

        vm.pickedRestoreFile("content://b.json")
        val pending = vm.status.value as BackupStatus.ConfirmRestore
        assertEquals("Wijk 07", pending.backup.snapshot.route.name)
        assertEquals("Something else", repo.route.value?.name) // nothing changed yet

        vm.confirmRestore()
        assertEquals(BackupStatus.Restored("Wijk 07", 25), vm.status.value)
        assertEquals(listOf("Kerkstraat", "Molenweg"), repo.segments.value.map { it.streetName })
        assertEquals(1, repo.buildings.value.size)
        assertTrue(settings.settings.value.onboardingCompleted)
    }

    @Test
    fun `cancel leaves everything as it was`() = runTest {
        route()
        vm.export("content://c.json")
        vm.pickedRestoreFile("content://c.json")
        vm.dismiss()
        assertEquals(BackupStatus.Idle, vm.status.value)
    }

    @Test
    fun `unreadable, foreign and failing restores are reported`() = runTest {
        vm.pickedRestoreFile("content://missing.json")
        assertEquals(BackupStatus.Failed(BackupError.READ_FAILED), vm.status.value)

        storage.files["content://photo.json"] = "{\"hello\":1}"
        vm.pickedRestoreFile("content://photo.json")
        assertEquals(BackupStatus.Failed(BackupError.NOT_A_BACKUP), vm.status.value)

        route()
        vm.export("content://d.json")
        vm.pickedRestoreFile("content://d.json")
        repo.failNextReplace = true
        vm.confirmRestore()
        assertEquals(BackupStatus.Failed(BackupError.RESTORE_FAILED), vm.status.value)
    }

    @Test
    fun `suggested file name contains the date`() {
        assertEquals("krantenwijk-backup-2026-09-30.json", vm.suggestedFileName("2026-09-30"))
    }
}
