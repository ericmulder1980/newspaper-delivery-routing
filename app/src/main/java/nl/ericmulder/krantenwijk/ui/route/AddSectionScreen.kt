package nl.ericmulder.krantenwijk.ui.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.rules.RangeIssue
import nl.ericmulder.krantenwijk.domain.rules.SectionPreview
import nl.ericmulder.krantenwijk.domain.rules.previewSample
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.MinTouchTarget
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.WizardProgress
import nl.ericmulder.krantenwijk.ui.common.label
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme

/** Add a street section with live preview (ADR-02, ADR-07, ADR-11). */
@Composable
fun AddSectionScreen(
    onBack: () -> Unit,
    onSaved: (segmentId: Long) -> Unit,
    /** Shows the setup wizard's progress (step 3) when adding streets during first launch. */
    wizardStep: Int? = null,
    viewModel: AddSectionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val savedSegmentId by viewModel.savedSegmentId.collectAsStateWithLifecycle()
    LaunchedEffect(savedSegmentId) { savedSegmentId?.let(onSaved) }

    ScreenScaffold(title = stringResource(R.string.add_section_title), onBack = onBack) {
        wizardStep?.let { WizardProgress(it) }
        Text(
            text = stringResource(R.string.add_section_kicker, state.sectionNumber),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StreetField(state.form.street, state.suggestions, viewModel::setStreet)
        SideToggle(state.form.side, viewModel::setSide)
        NumberFields(state.form.from, state.form.to, viewModel::setFrom, viewModel::setTo)
        ReverseOption(state.form.reverse, viewModel::setReverse)
        Preview(state.preview)
        if (state.saveFailed) {
            Text(
                text = stringResource(R.string.add_section_save_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        AccentButton(
            text = stringResource(R.string.add_section_save),
            onClick = viewModel::save,
            enabled = state.canSave,
        )
    }
}

@Composable
private fun StreetField(street: String, suggestions: List<String>, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = street,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.street_label)) },
        placeholder = { Text(stringResource(R.string.street_hint)) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
    if (suggestions.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.street_suggestions), style = MaterialTheme.typography.bodyMedium)
            suggestions.forEach { name ->
                OutlinedButton(onClick = { onChange(name) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(name) }
            }
        }
    }
}

@Composable
private fun SideToggle(selected: Side, onSelect: (Side) -> Unit) {
    Text(stringResource(R.string.side_question), style = MaterialTheme.typography.titleMedium)
    val colors = KrantenwijkTheme.colors
    Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Side.entries.forEach { side ->
            val on = side == selected
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (on) colors.accentFill else MaterialTheme.colorScheme.surfaceContainer,
                contentColor = if (on) colors.onAccentFill else MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(2.dp, if (on) colors.accentFillBorder else MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MinTouchTarget)
                    .selectable(selected = on, role = Role.RadioButton, onClick = { onSelect(side) }),
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(side.label()),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberFields(from: String, to: String, onFrom: (String) -> Unit, onTo: (String) -> Unit) {
    Text(stringResource(R.string.numbers_label), style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NumberField(from, onFrom, stringResource(R.string.numbers_from), "2", ImeAction.Next, Modifier.weight(1f))
        NumberField(to, onTo, stringResource(R.string.numbers_to), "24", ImeAction.Done, Modifier.weight(1f))
    }
}

@Composable
private fun NumberField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    hint: String,
    imeAction: ImeAction,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = { Text(hint, style = MaterialTheme.typography.displaySmall) },
        singleLine = true,
        textStyle = MaterialTheme.typography.displaySmall,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        modifier = modifier,
    )
}

@Composable
private fun ReverseOption(reverse: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .toggleable(value = reverse, role = Role.Checkbox, onValueChange = onChange),
    ) {
        Checkbox(checked = reverse, onCheckedChange = null)
        Text(
            text = stringResource(R.string.reverse_label),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun Preview(preview: SectionPreview) {
    val warning = preview.issues.firstNotNullOfOrNull { it.message() }
    if (preview.numbers.isNotEmpty()) {
        val first = preview.numbers.first()
        val last = preview.numbers.last()
        val direction = if (first <= last) {
            stringResource(R.string.preview_ascending, first, last)
        } else {
            stringResource(R.string.preview_reverse, first, last)
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = pluralStringResource(R.plurals.preview_count, preview.numbers.size, preview.numbers.size) +
                        " · " + direction,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = sampleText(previewSample(preview.numbers)),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (warning != null) {
        Text(
            text = warning,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

/** "2, 4, 6 … 24": null in the sample marks the gap. */
private fun sampleText(sample: List<Int?>): String = buildString {
    sample.forEachIndexed { i, n ->
        when {
            n == null -> append(" …")
            i == 0 -> append(n)
            sample[i - 1] == null -> append(" ").append(n)
            else -> append(", ").append(n)
        }
    }
}

/** Text shown for a range issue; null when nothing should be shown (e.g. fields still empty). */
@Composable
private fun RangeIssue.message(): String? = when (this) {
    RangeIssue.InvalidNumber -> null
    RangeIssue.FromAfterTo -> stringResource(R.string.range_from_after_to)
    RangeIssue.NoNumbers -> stringResource(R.string.range_no_numbers)
    is RangeIssue.TooManyNumbers -> stringResource(R.string.range_too_many)
    is RangeIssue.EndpointNotOnSide -> when (side) {
        Side.EVEN -> stringResource(R.string.range_even_side)
        Side.ODD -> stringResource(R.string.range_odd_side)
        Side.ALL -> null
    }
}
