package nl.ericmulder.krantenwijk.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.data.settings.DataStoreSettingsRepository
import nl.ericmulder.krantenwijk.domain.model.AppSettings
import nl.ericmulder.krantenwijk.domain.model.ThemeMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SettingsRepositoryTest {

    @TempDir
    lateinit var dir: File

    private fun repository() = DataStoreSettingsRepository(
        DataStoreSettingsRepository.createDataStore(dir.resolve(DataStoreSettingsRepository.FILE_NAME).absolutePath),
    )

    @Test
    fun `defaults follow the requirements`() = runTest {
        val settings = repository().settings.first()
        assertEquals(AppSettings(), settings)
        assertEquals(false, settings.showNonExisting) // ADR-04: hidden by default
        assertEquals(true, settings.showSkipped) // RND-04: shown by default
        assertEquals(true, settings.keepScreenOn) // RND-08
        assertEquals(ThemeMode.SYSTEM, settings.theme)
    }

    @Test
    fun `changes are stored`() = runTest {
        val repo = repository()
        repo.setNickname("  Eric ")
        repo.setOnboardingCompleted(true)
        repo.setTheme(ThemeMode.DARK)
        repo.setShowNonExisting(true)
        repo.setShowSkipped(false)
        repo.setKeepScreenOn(false)

        assertEquals(
            AppSettings(
                nickname = "Eric",
                onboardingCompleted = true,
                theme = ThemeMode.DARK,
                showNonExisting = true,
                showSkipped = false,
                keepScreenOn = false,
            ),
            repo.settings.first(),
        )
    }

    @Test
    fun `blank nickname clears it`() = runTest {
        val repo = repository()
        repo.setNickname("Eric")
        repo.setNickname(" ")
        assertNull(repo.settings.first().nickname)
    }
}
