package nl.ericmulder.krantenwijk.ui.update

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.BuildConfig
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.data.update.InstallEvent
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton

/** "App updates" in Settings (REL-C): version, check, download and install. */
@Composable
fun UpdateSection(viewModel: UpdateViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val installEvent by viewModel.installEvents.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Text(
        stringResource(R.string.settings_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(stringResource(R.string.update_privacy), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

    when (val s = state) {
        UpdateState.Idle, UpdateState.UpToDate, is UpdateState.Failed -> {
            if (s == UpdateState.UpToDate) Status(stringResource(R.string.update_up_to_date))
            if (s is UpdateState.Failed) Status(s.reason.message(s.detail), error = true)
            SecondaryButton(stringResource(R.string.update_check), viewModel::check)
        }
        UpdateState.Checking -> Status(stringResource(R.string.update_checking))
        is UpdateState.Available -> {
            Status(stringResource(R.string.update_available, s.release.versionName))
            Text(s.release.notes.take(NOTES_PREVIEW), style = MaterialTheme.typography.bodyMedium)
            AccentButton(stringResource(R.string.update_install), viewModel::install)
        }
        is UpdateState.NeedsPermission -> {
            Status(stringResource(R.string.update_permission_explain))
            SecondaryButton(stringResource(R.string.update_open_settings), { context.startActivity(viewModel.permissionSettingsIntent()) })
            AccentButton(stringResource(R.string.update_install), viewModel::install)
        }
        is UpdateState.Downloading -> {
            Status(stringResource(R.string.update_downloading, s.release.versionName, (s.progress * 100).toInt()))
            LinearProgressIndicator(progress = { s.progress }, modifier = Modifier.fillMaxWidth())
        }
        is UpdateState.Installing -> when (val e = installEvent) {
            is InstallEvent.Failed -> {
                Status(UpdateFailure.INSTALL.message(e.message), error = true)
                SecondaryButton(stringResource(R.string.update_check), viewModel::check)
            }
            InstallEvent.WaitingForUser -> Status(stringResource(R.string.update_confirm_system))
            InstallEvent.None -> Status(stringResource(R.string.update_installing, s.release.versionName))
        }
    }
}

private const val NOTES_PREVIEW = 600

@Composable
private fun Status(text: String, error: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
private fun UpdateFailure.message(detail: String?): String = when (this) {
    UpdateFailure.OFFLINE -> stringResource(R.string.update_error_offline)
    UpdateFailure.SERVER -> stringResource(R.string.update_error_server)
    UpdateFailure.NO_RELEASE -> stringResource(R.string.update_error_no_release)
    UpdateFailure.CHECKSUM -> stringResource(R.string.update_error_checksum)
    UpdateFailure.TOO_LARGE -> stringResource(R.string.update_error_server)
    UpdateFailure.INSTALL -> stringResource(R.string.update_error_install, detail ?: "–")
}
