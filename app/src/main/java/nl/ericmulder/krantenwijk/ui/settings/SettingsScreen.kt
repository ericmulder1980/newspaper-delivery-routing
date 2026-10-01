package nl.ericmulder.krantenwijk.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import nl.ericmulder.krantenwijk.ui.backup.BackupSection
import nl.ericmulder.krantenwijk.ui.update.UpdateSection
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.BuildConfig
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.ThemeMode
import nl.ericmulder.krantenwijk.ui.common.MinTouchTarget
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold

@StringRes
private fun ThemeMode.label(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

/** Settings (plan §6). This feature adds the theme (SET-01) and version info (SET-02); more options follow. */
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    ScreenScaffold(title = stringResource(R.string.settings_title), onBack = onBack) {
        val prefs by viewModel.round.collectAsStateWithLifecycle()
        val route by viewModel.route.collectAsStateWithLifecycle()
        prefs?.let { NameSection(it.nickname.orEmpty(), viewModel::setNickname) }
        route?.let { RouteSection(it.name, it.town.orEmpty(), viewModel::saveRoute) }
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Column(Modifier.selectableGroup()) {
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MinTouchTarget)
                        .selectable(selected = theme == mode, role = Role.RadioButton, onClick = { viewModel.setTheme(mode) }),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = theme == mode, onClick = null)
                    Text(
                        text = stringResource(mode.label()),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
        }
        prefs?.let { prefs ->
            Text(
                text = stringResource(R.string.settings_round),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp).semantics { heading() },
            )
            SwitchRow(stringResource(R.string.settings_show_skipped), prefs.showSkipped, viewModel::setShowSkipped)
            SwitchRow(stringResource(R.string.settings_show_non_existing), prefs.showNonExisting, viewModel::setShowNonExisting)
            SwitchRow(stringResource(R.string.settings_keep_screen_on), prefs.keepScreenOn, viewModel::setKeepScreenOn)
        }
        SectionHeading(stringResource(R.string.backup_title))
        BackupSection()
        SectionHeading(stringResource(R.string.update_title))
        UpdateSection()
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** "Your name" (ADR-01: editable later). Saved when tapping Save. */
@Composable
private fun NameSection(current: String, onSave: (String) -> Unit) {
    var name by rememberSaveable(current) { mutableStateOf(current) }
    SectionHeading(stringResource(R.string.settings_you))
    OutlinedTextField(
        value = name,
        onValueChange = { name = it.take(40) },
        label = { Text(stringResource(R.string.onboarding_name_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
    )
    SecondaryButton(stringResource(R.string.save), { onSave(name) }, enabled = name.trim() != current)
}

/** Route name and town (ADR-01: editable later). */
@Composable
private fun RouteSection(currentName: String, currentTown: String, onSave: (String, String) -> Unit) {
    var name by rememberSaveable(currentName) { mutableStateOf(currentName) }
    var town by rememberSaveable(currentTown) { mutableStateOf(currentTown) }
    SectionHeading(stringResource(R.string.settings_route))
    OutlinedTextField(
        value = name,
        onValueChange = { name = it.take(40) },
        label = { Text(stringResource(R.string.route_name_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = town,
        onValueChange = { town = it.take(40) },
        label = { Text(stringResource(R.string.route_town_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    SecondaryButton(
        stringResource(R.string.save),
        { onSave(name, town) },
        enabled = name.isNotBlank() && (name.trim() != currentName || town.trim() != currentTown),
    )
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 12.dp).semantics { heading() },
    )
}
