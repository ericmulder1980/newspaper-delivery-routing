package nl.ericmulder.krantenwijk.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.ericmulder.krantenwijk.domain.model.AppSettings
import nl.ericmulder.krantenwijk.domain.model.ThemeMode
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository
import okio.Path.Companion.toPath

class DataStoreSettingsRepository(private val dataStore: DataStore<Preferences>) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            nickname = prefs[NICKNAME],
            onboardingCompleted = prefs[ONBOARDING_COMPLETED] ?: defaults.onboardingCompleted,
            theme = prefs[THEME]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: defaults.theme,
            showNonExisting = prefs[SHOW_NON_EXISTING] ?: defaults.showNonExisting,
            showSkipped = prefs[SHOW_SKIPPED] ?: defaults.showSkipped,
            keepScreenOn = prefs[KEEP_SCREEN_ON] ?: defaults.keepScreenOn,
        )
    }

    override suspend fun setNickname(nickname: String?) {
        val clean = nickname?.trim()?.ifEmpty { null }
        dataStore.edit { if (clean == null) it.remove(NICKNAME) else it[NICKNAME] = clean }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) = set(ONBOARDING_COMPLETED, completed)

    override suspend fun setTheme(theme: ThemeMode) = set(THEME, theme.name)

    override suspend fun setShowNonExisting(show: Boolean) = set(SHOW_NON_EXISTING, show)

    override suspend fun setShowSkipped(show: Boolean) = set(SHOW_SKIPPED, show)

    override suspend fun setKeepScreenOn(keepOn: Boolean) = set(KEEP_SCREEN_ON, keepOn)

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    companion object {
        const val FILE_NAME = "settings.preferences_pb"

        private val NICKNAME = stringPreferencesKey("nickname")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val THEME = stringPreferencesKey("theme")
        private val SHOW_NON_EXISTING = booleanPreferencesKey("show_non_existing")
        private val SHOW_SKIPPED = booleanPreferencesKey("show_skipped")
        private val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")

        /**
         * Creates the settings DataStore at [path]; call once per file per process. Tests pass their
         * own [scope] so they can close it and reopen the file, as after a restart.
         */
        fun createDataStore(
            path: String,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        ): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { path.toPath() })
    }
}
