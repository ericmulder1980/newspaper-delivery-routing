package nl.ericmulder.krantenwijk.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.BuildConfig
import nl.ericmulder.krantenwijk.data.BackupStorage
import nl.ericmulder.krantenwijk.data.backup.BackupException
import nl.ericmulder.krantenwijk.data.backup.BackupFormat
import nl.ericmulder.krantenwijk.data.backup.DecodedBackup
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Named

/** What the backup UI shows (DATA-02). */
sealed interface BackupStatus {
    data object Idle : BackupStatus

    data object Working : BackupStatus

    data class Exported(val sections: Int, val addresses: Int) : BackupStatus

    /** A file was read; waiting for the user to confirm that it replaces the current route. */
    data class ConfirmRestore(val backup: DecodedBackup, val uri: String) : BackupStatus

    data class Restored(val routeName: String, val addresses: Int) : BackupStatus

    data class Failed(val error: BackupError) : BackupStatus
}

enum class BackupError { NO_ROUTE, WRITE_FAILED, READ_FAILED, NOT_A_BACKUP, TOO_NEW, DAMAGED, RESTORE_FAILED }

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val routes: RouteRepository,
    private val settings: SettingsRepository,
    private val storage: BackupStorage,
    @Named("clock") private val clock: @JvmSuppressWildcards () -> Long,
) : ViewModel() {

    private val _status = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val status: StateFlow<BackupStatus> = _status.asStateFlow()

    /** Suggested file name, e.g. "krantenwijk-backup-2026-09-30.json". */
    fun suggestedFileName(today: String): String = "krantenwijk-backup-$today.json"

    fun export(uri: String) {
        _status.value = BackupStatus.Working
        viewModelScope.launch {
            val snapshot = routes.snapshot()
            if (snapshot == null) {
                _status.value = BackupStatus.Failed(BackupError.NO_ROUTE)
                return@launch
            }
            _status.value = try {
                storage.write(uri, BackupFormat.encode(snapshot, clock(), BuildConfig.VERSION_NAME))
                BackupStatus.Exported(snapshot.sections.size, snapshot.addressCount)
            } catch (e: Exception) {
                BackupStatus.Failed(BackupError.WRITE_FAILED)
            }
        }
    }

    /** Reads and checks [uri]; the restore itself waits for [confirmRestore]. */
    fun pickedRestoreFile(uri: String) {
        _status.value = BackupStatus.Working
        viewModelScope.launch {
            _status.value = try {
                BackupStatus.ConfirmRestore(BackupFormat.decode(storage.read(uri)), uri)
            } catch (e: BackupException) {
                BackupStatus.Failed(
                    when (e.reason) {
                        BackupException.Reason.NOT_A_BACKUP -> BackupError.NOT_A_BACKUP
                        BackupException.Reason.TOO_NEW -> BackupError.TOO_NEW
                        BackupException.Reason.DAMAGED -> BackupError.DAMAGED
                    },
                )
            } catch (e: Exception) {
                BackupStatus.Failed(BackupError.READ_FAILED)
            }
        }
    }

    /** Replaces the current route with the backup; also completes first-launch setup. */
    fun confirmRestore() {
        val pending = _status.value as? BackupStatus.ConfirmRestore ?: return
        _status.value = BackupStatus.Working
        viewModelScope.launch {
            _status.value = try {
                routes.replaceAll(pending.backup.snapshot)
                settings.setOnboardingCompleted(true)
                BackupStatus.Restored(pending.backup.snapshot.route.name, pending.backup.snapshot.addressCount)
            } catch (e: Exception) {
                BackupStatus.Failed(BackupError.RESTORE_FAILED)
            }
        }
    }

    fun dismiss() {
        _status.value = BackupStatus.Idle
    }
}
