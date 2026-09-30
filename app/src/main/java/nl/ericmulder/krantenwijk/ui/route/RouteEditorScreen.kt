package nl.ericmulder.krantenwijk.ui.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.PrimaryActionHeight
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.label

/**
 * Route editor. For now: create the route, list its sections and add new ones (ADR-A).
 * Reordering and direction follow in ADR-C.
 */
@Composable
fun RouteEditorScreen(
    onBack: () -> Unit,
    onAddSection: () -> Unit,
    onOpenSection: (segmentId: Long) -> Unit,
    viewModel: RouteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenScaffold(title = stringResource(R.string.route_editor_title), onBack = onBack) {
        when (val s = state) {
            RouteEditorUiState.Loading -> Unit
            RouteEditorUiState.NoRoute -> CreateRouteForm(onCreate = viewModel::createRoute)
            is RouteEditorUiState.Ready -> {
                Text(s.route.name, style = MaterialTheme.typography.headlineLarge)
                s.route.town?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (s.segments.isEmpty()) {
                    Text(
                        stringResource(R.string.route_no_sections),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                s.segments.forEachIndexed { index, segment ->
                    SegmentRow(
                        position = index + 1,
                        segment = segment,
                        addressCount = s.addressCounts[segment.id] ?: 0,
                        onClick = { onOpenSection(segment.id) },
                    )
                }
                AccentButton(text = stringResource(R.string.route_add_section), onClick = onAddSection)
            }
        }
    }
}

@Composable
private fun CreateRouteForm(onCreate: (name: String, town: String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var town by rememberSaveable { mutableStateOf("") }
    Text(stringResource(R.string.route_create_intro), style = MaterialTheme.typography.bodyLarge)
    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text(stringResource(R.string.route_name_label)) },
        placeholder = { Text(stringResource(R.string.route_name_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = town,
        onValueChange = { town = it },
        label = { Text(stringResource(R.string.route_town_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
    )
    AccentButton(
        text = stringResource(R.string.route_create),
        onClick = { onCreate(name, town) },
        enabled = name.isNotBlank(),
    )
}

@Composable
private fun SegmentRow(position: Int, segment: Segment, addressCount: Int, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().heightIn(min = PrimaryActionHeight).clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("$position. ${segment.streetName}", style = MaterialTheme.typography.titleMedium)
            val range = if (segment.direction == Direction.DESCENDING) {
                stringResource(R.string.section_range_reverse, segment.rangeTo, segment.rangeFrom)
            } else {
                stringResource(R.string.section_range, segment.rangeFrom, segment.rangeTo)
            }
            Text(
                text = stringResource(segment.side.label()) + " · " + range + " · " +
                    pluralStringResource(R.plurals.address_count, addressCount, addressCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
