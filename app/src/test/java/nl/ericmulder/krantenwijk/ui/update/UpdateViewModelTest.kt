package nl.ericmulder.krantenwijk.ui.update

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.data.update.AppInstaller
import nl.ericmulder.krantenwijk.data.update.AvailableRelease
import nl.ericmulder.krantenwijk.data.update.InstallEvent
import nl.ericmulder.krantenwijk.data.update.UpdateException
import nl.ericmulder.krantenwijk.data.update.UpdateSource
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UpdateViewModelTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    @TempDir
    lateinit var dir: File

    private val v12 = AvailableRelease("0.12.0", 13, "https://x/krantenwijk-0.12.0.apk", 100, "https://x/s.sha256", "Notes")

    private class FakeSource : UpdateSource {
        var latest: AvailableRelease? = null
        var checkError: UpdateException.Reason? = null
        var downloadError: UpdateException.Reason? = null
        var downloads = 0

        override suspend fun latestRelease(): AvailableRelease {
            checkError?.let { throw UpdateException(it) }
            return latest ?: throw UpdateException(UpdateException.Reason.NO_RELEASE)
        }

        override suspend fun download(release: AvailableRelease, target: File, onProgress: (Float) -> Unit) {
            downloadError?.let { throw UpdateException(it) }
            downloads++
            onProgress(0.5f)
            target.writeText("apk")
            onProgress(1f)
        }
    }

    private class FakeInstaller : AppInstaller {
        override val events: StateFlow<InstallEvent> = MutableStateFlow(InstallEvent.None)
        var allowed = true
        val installed = mutableListOf<File>()

        override fun canInstall() = allowed

        override fun permissionSettingsIntent(): Intent = throw UnsupportedOperationException()

        override suspend fun install(apk: File) {
            installed += apk
        }
    }

    private val source = FakeSource()
    private val installer = FakeInstaller()
    private val vm by lazy { UpdateViewModel(source, installer, currentVersionCode = 12, updateDir = dir) }

    @Test
    fun `up to date when the latest release is not newer`() = runTest {
        source.latest = v12.copy(versionCode = 12)
        vm.check()
        assertEquals(UpdateState.UpToDate, vm.state.value)
    }

    @Test
    fun `newer release is offered, downloaded, verified and installed`() = runTest {
        source.latest = v12
        vm.check()
        assertEquals(UpdateState.Available(v12), vm.state.value)
        vm.install()
        assertEquals(UpdateState.Installing(v12), vm.state.value)
        assertEquals("krantenwijk-0.12.0.apk", installer.installed.single().name)
    }

    @Test
    fun `without install permission the user is sent to the setting first`() = runTest {
        source.latest = v12
        installer.allowed = false
        vm.check()
        vm.install()
        assertEquals(UpdateState.NeedsPermission(v12), vm.state.value)
        assertEquals(0, source.downloads)

        installer.allowed = true
        vm.install()
        assertEquals(1, installer.installed.size)
    }

    @Test
    fun `failures are reported and nothing is installed`() = runTest {
        source.checkError = UpdateException.Reason.OFFLINE
        vm.check()
        assertEquals(UpdateState.Failed(UpdateFailure.OFFLINE), vm.state.value)

        source.checkError = null
        source.latest = v12
        source.downloadError = UpdateException.Reason.CHECKSUM
        vm.check()
        vm.install()
        assertEquals(UpdateState.Failed(UpdateFailure.CHECKSUM), vm.state.value)
        assertTrue(installer.installed.isEmpty())
    }

    @Test
    fun `old downloads are removed before a new one`() = runTest {
        File(dir, "krantenwijk-0.10.0.apk").writeText("old")
        source.latest = v12
        vm.check()
        vm.install()
        assertFalse(File(dir, "krantenwijk-0.10.0.apk").exists())
        assertTrue(File(dir, "krantenwijk-0.12.0.apk").exists())
    }
}
