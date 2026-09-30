package nl.ericmulder.krantenwijk.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.domain.model.AppSettings
import nl.ericmulder.krantenwijk.domain.model.ThemeMode
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val settings: SettingsRepository) : ViewModel() {

    val theme: StateFlow<ThemeMode?> = settings.settings
        .map { it.theme }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    val round: StateFlow<AppSettings?> = settings.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    fun setShowSkipped(show: Boolean) {
        viewModelScope.launch { settings.setShowSkipped(show) }
    }

    fun setShowNonExisting(show: Boolean) {
        viewModelScope.launch { settings.setShowNonExisting(show) }
    }

    fun setKeepScreenOn(keepOn: Boolean) {
        viewModelScope.launch { settings.setKeepScreenOn(keepOn) }
    }

    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch { settings.setTheme(theme) }
    }
}
