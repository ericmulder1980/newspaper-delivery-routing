package nl.ericmulder.krantenwijk.ui.onboarding

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.ui.backup.BackupSection
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton
import nl.ericmulder.krantenwijk.ui.common.WizardProgress
import nl.ericmulder.krantenwijk.ui.route.ReorderableSections

/** Step 1: "What should we call you?" */
@Composable
fun OnboardingNameScreen(
    onNext: () -> Unit,
    onRestored: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel(),
    /** Hidden in screen tests, which have no Hilt; the restore flow is tested separately. */
    showRestore: Boolean = true,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    // Prefill when returning to this step.
    LaunchedEffect(state?.nickname) { if (name.isEmpty()) state?.nickname?.let { name = it } }
    ScreenScaffold(title = stringResource(R.string.onboarding_title), onBack = null) {
        WizardProgress(step = 1)
        Text(stringResource(R.string.onboarding_name_question), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(R.string.onboarding_name_intro), style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(40) },
            label = { Text(stringResource(R.string.onboarding_name_label)) },
            placeholder = { Text(stringResource(R.string.onboarding_name_hint)) },
            supportingText = { Text(stringResource(R.string.onboarding_name_local)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
        AccentButton(
            text = stringResource(R.string.onboarding_next),
            onClick = { viewModel.saveName(name, onNext) },
            enabled = name.isNotBlank(),
        )
        // New phone: restore the route from a backup instead of entering it again (DATA-02).
        if (showRestore) BackupSection(restoreOnly = true, onRestored = onRestored)
    }
}

/** Step 2: "Hi Sam!" – route name and town. */
@Composable
fun OnboardingRouteScreen(onBack: () -> Unit, onNext: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var town by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state?.route) {
        state?.route?.let { route ->
            if (name.isEmpty()) name = route.name
            if (town.isEmpty()) town = route.town.orEmpty()
        }
    }
    ScreenScaffold(title = stringResource(R.string.onboarding_route_title), onBack = onBack) {
        WizardProgress(step = 2)
        Text(
            state?.nickname?.let { stringResource(R.string.onboarding_greeting, it) } ?: "",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(stringResource(R.string.onboarding_route_intro), style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(40) },
            label = { Text(stringResource(R.string.route_name_label)) },
            placeholder = { Text(stringResource(R.string.route_name_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = town,
            onValueChange = { town = it.take(40) },
            label = { Text(stringResource(R.string.route_town_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
        AccentButton(
            text = stringResource(R.string.onboarding_add_streets),
            onClick = { viewModel.saveRoute(name, town, onNext) },
            enabled = name.isNotBlank(),
        )
    }
}

/** Step 4: the walking route so far; add more sections or finish. */
@Composable
fun OnboardingSectionsScreen(
    onBack: () -> Unit,
    onAddSection: () -> Unit,
    onOpenSection: (Long) -> Unit,
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenScaffold(title = stringResource(R.string.onboarding_sections_title), onBack = onBack) {
        WizardProgress(step = 4)
        Text(stringResource(R.string.onboarding_sections_intro), style = MaterialTheme.typography.bodyLarge)
        val ready = state ?: return@ScreenScaffold
        if (ready.segments.size > 1) {
            Text(stringResource(R.string.reorder_hint), style = MaterialTheme.typography.bodyMedium)
        }
        ReorderableSections(
            segments = ready.segments,
            detail = { segment ->
                val count = ready.addressCounts[segment.id] ?: 0
                pluralStringResource(R.plurals.address_count, count, count)
            },
            onOpen = { onOpenSection(it.id) },
            onReorder = viewModel::reorder,
        )
        SecondaryButton(stringResource(R.string.route_add_section), onAddSection)
        AccentButton(
            text = stringResource(R.string.onboarding_done),
            onClick = { viewModel.finish(onFinished) },
            enabled = ready.segments.isNotEmpty(),
        )
    }
}
