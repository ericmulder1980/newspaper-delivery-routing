package nl.ericmulder.krantenwijk.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.ericmulder.krantenwijk.domain.model.AppSettings
import nl.ericmulder.krantenwijk.domain.model.ThemeMode

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setNickname(nickname: String?)

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setTheme(theme: ThemeMode)

    suspend fun setShowNonExisting(show: Boolean)

    suspend fun setShowSkipped(show: Boolean)

    suspend fun setKeepScreenOn(keepOn: Boolean)
}
