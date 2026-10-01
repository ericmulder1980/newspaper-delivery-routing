package nl.ericmulder.krantenwijk.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.data.update.AppInstaller
import nl.ericmulder.krantenwijk.data.update.AvailableRelease
import nl.ericmulder.krantenwijk.data.update.InstallEvent
import nl.ericmulder.krantenwijk.data.update.UpdateException
import nl.ericmulder.krantenwijk.data.update.UpdateSource
import nl.ericmulder.krantenwijk.data.update.isUpdate
import java.io.File
import javax.inject.Inject
import javax.inject.Named

sealed interface UpdateState {
    data object Idle : UpdateState

    data object Checking : UpdateState

    data object UpToDate : UpdateState

    data class Available(val release: AvailableRelease) : UpdateState

    /** The user still has to allow "install unknown apps" for Krantenwijk. */
    data class NeedsPermission(val release: AvailableRelease) : UpdateState

    data class Downloading(val release: AvailableRelease, val progress: Float) : UpdateState

    data class Installing(val release: AvailableRelease) : UpdateState

    data class Failed(val reason: UpdateFailure, val detail: String? = null) : UpdateState
}

enum class UpdateFailure { OFFLINE, SERVER, NO_RELEASE, CHECKSUM, TOO_LARGE, INSTALL }

/** "Check for updates" in Settings (REL-C, DEC-024). Only runs when the user taps. */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val source: UpdateSource,
    private val installer: AppInstaller,
    @Named("versionCode") private val currentVersionCode: Int,
    @Named("updateDir") private val updateDir: File,
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    /** Installer feedback (confirmation screen, failure); the UI shows it while installing. */
    val installEvents: StateFlow<InstallEvent> = installer.events

    fun check() {
        _state.value = UpdateState.Checking
        viewModelScope.launch {
            _state.value = try {
                val latest = source.latestRelease()
                if (isUpdate(latest, currentVersionCode)) UpdateState.Available(latest) else UpdateState.UpToDate
            } catch (e: UpdateException) {
                UpdateState.Failed(e.reason.toFailure())
            }
        }
    }

    /** Download, verify and install; asks for the install permission first if needed. */
    fun install() {
        val release = when (val s = _state.value) {
            is UpdateState.Available -> s.release
            is UpdateState.NeedsPermission -> s.release
            else -> return
        }
        if (!installer.canInstall()) {
            _state.value = UpdateState.NeedsPermission(release)
            return
        }
        _state.value = UpdateState.Downloading(release, 0f)
        viewModelScope.launch {
            val apk = File(updateDir, "krantenwijk-${release.versionName}.apk")
            try {
                updateDir.listFiles()?.forEach { it.delete() } // only the newest download is kept
                source.download(release, apk) { progress -> _state.value = UpdateState.Downloading(release, progress) }
                _state.value = UpdateState.Installing(release)
                installer.install(apk)
            } catch (e: UpdateException) {
                _state.value = UpdateState.Failed(e.reason.toFailure())
            } catch (e: Exception) {
                _state.value = UpdateState.Failed(UpdateFailure.INSTALL, e.message)
            }
        }
    }

    fun permissionSettingsIntent() = installer.permissionSettingsIntent()

    private fun UpdateException.Reason.toFailure() = when (this) {
        UpdateException.Reason.OFFLINE -> UpdateFailure.OFFLINE
        UpdateException.Reason.SERVER -> UpdateFailure.SERVER
        UpdateException.Reason.NO_RELEASE -> UpdateFailure.NO_RELEASE
        UpdateException.Reason.CHECKSUM -> UpdateFailure.CHECKSUM
        UpdateException.Reason.TOO_LARGE -> UpdateFailure.TOO_LARGE
    }
}
