package nl.ericmulder.krantenwijk.ui.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nl.ericmulder.krantenwijk.domain.model.AppSettings
import nl.ericmulder.krantenwijk.domain.model.ThemeMode
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    override val settings = MutableStateFlow(initial)

    override suspend fun setNickname(nickname: String?) = settings.update { it.copy(nickname = nickname) }

    override suspend fun setOnboardingCompleted(completed: Boolean) = settings.update { it.copy(onboardingCompleted = completed) }

    override suspend fun setTheme(theme: ThemeMode) = settings.update { it.copy(theme = theme) }

    override suspend fun setShowNonExisting(show: Boolean) = settings.update { it.copy(showNonExisting = show) }

    override suspend fun setShowSkipped(show: Boolean) = settings.update { it.copy(showSkipped = show) }

    override suspend fun setKeepScreenOn(keepOn: Boolean) = settings.update { it.copy(keepScreenOn = keepOn) }
}
