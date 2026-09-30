package nl.ericmulder.krantenwijk.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.data.backup.BackupFormat
import nl.ericmulder.krantenwijk.ui.common.MinTouchTarget
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date

/**
 * Backup and restore through the system file picker (DATA-02). In Settings both actions are shown;
 * in the setup wizard only restore ([restoreOnly]), and [onRestored] continues to the home screen.
 */
@Composable
fun BackupSection(
    restoreOnly: Boolean = false,
    onRestored: () -> Unit = {},
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupFormat.MIME_TYPE)) { uri ->
        uri?.let { viewModel.export(it.toString()) }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.pickedRestoreFile(it.toString()) }
    }
    val pickRestoreFile = {
        // Some file providers label .json files as generic binaries or text.
        restoreLauncher.launch(arrayOf(BackupFormat.MIME_TYPE, "application/octet-stream", "text/*"))
    }

    LaunchedEffect(status) { if (status is BackupStatus.Restored && restoreOnly) onRestored() }

    if (restoreOnly) {
        TextButton(onClick = pickRestoreFile, modifier = Modifier.fillMaxWidth().heightIn(min = MinTouchTarget)) {
            Text(stringResource(R.string.backup_restore_existing), style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Text(stringResource(R.string.backup_explanation), style = MaterialTheme.typography.bodyMedium)
        SecondaryButton(
            stringResource(R.string.backup_save),
            { exportLauncher.launch(viewModel.suggestedFileName(LocalDate.now().toString())) },
        )
        SecondaryButton(stringResource(R.string.backup_restore), pickRestoreFile)
    }
    StatusLine(status)

    (status as? BackupStatus.ConfirmRestore)?.let { pending ->
        val snapshot = pending.backup.snapshot
        val date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(pending.backup.exportedAtMillis))
        AlertDialog(
            onDismissRequest = viewModel::dismiss,
            title = { Text(stringResource(R.string.backup_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.backup_confirm_text,
                        snapshot.route.name,
                        pluralStringResource(R.plurals.section_count, snapshot.sections.size, snapshot.sections.size),
                        pluralStringResource(R.plurals.address_count, snapshot.addressCount, snapshot.addressCount),
                        date,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmRestore) {
                    Text(stringResource(R.string.backup_confirm_restore), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismiss) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun StatusLine(status: BackupStatus) {
    val (text, isError) = when (status) {
        BackupStatus.Idle, is BackupStatus.ConfirmRestore -> return
        BackupStatus.Working -> stringResource(R.string.backup_working) to false
        is BackupStatus.Exported -> stringResource(
            R.string.backup_saved,
            pluralStringResource(R.plurals.section_count, status.sections, status.sections),
            pluralStringResource(R.plurals.address_count, status.addresses, status.addresses),
        ) to false
        is BackupStatus.Restored -> stringResource(
            R.string.backup_restored,
            status.routeName,
            pluralStringResource(R.plurals.address_count, status.addresses, status.addresses),
        ) to false
        is BackupStatus.Failed -> stringResource(
            when (status.error) {
                BackupError.NO_ROUTE -> R.string.backup_error_no_route
                BackupError.WRITE_FAILED -> R.string.backup_error_write
                BackupError.READ_FAILED -> R.string.backup_error_read
                BackupError.NOT_A_BACKUP -> R.string.backup_error_not_a_backup
                BackupError.TOO_NEW -> R.string.backup_error_too_new
                BackupError.DAMAGED -> R.string.backup_error_damaged
                BackupError.RESTORE_FAILED -> R.string.backup_error_restore
            },
        ) to true
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}
