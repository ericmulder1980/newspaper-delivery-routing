package nl.ericmulder.krantenwijk.ui.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.MinTouchTarget
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.dashedBorder
import nl.ericmulder.krantenwijk.ui.common.label
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme

private const val COLUMNS = 4

private fun Address.label() = "$houseNumber${addition.orEmpty()}"

/** Number check and address editing for one street section (ADR-12, ADR-03, ADR-04). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentDetailScreen(
    segmentId: Long,
    onBack: () -> Unit,
    viewModel: SegmentDetailViewModel = hiltViewModel<SegmentDetailViewModel, SegmentDetailViewModel.Factory>(
        key = "segment-$segmentId",
        creationCallback = { it.create(segmentId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val addError by viewModel.addError.collectAsStateWithLifecycle()
    val added by viewModel.added.collectAsStateWithLifecycle()

    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state) { if (state == SegmentDetailUiState.Gone) onBack() }
    LaunchedEffect(added) { if (added > 0) adding = false }

    val ready = state as? SegmentDetailUiState.Ready
    ScreenScaffold(title = ready?.segment?.streetName ?: "", onBack = onBack) {
        if (ready == null) return@ScreenScaffold
        val segment = ready.segment
        Text(
            text = stringResource(R.string.segment_kicker, ready.position),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val range = if (segment.direction == Direction.DESCENDING) {
            stringResource(R.string.section_range_reverse, segment.rangeTo, segment.rangeFrom)
        } else {
            stringResource(R.string.section_range, segment.rangeFrom, segment.rangeTo)
        }
        Text(
            text = stringResource(segment.side.label()) + " · " + range + " · " +
                pluralStringResource(R.plurals.address_count, ready.existingCount, ready.existingCount),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.segment_tap_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        NumberGrid(
            addresses = ready.addresses,
            onTap = { selectedId = it.id },
            onAdd = {
                viewModel.clearAddError()
                adding = true
            },
        )
        AccentButton(text = stringResource(R.string.segment_done), onClick = onBack)
        TextButton(
            onClick = { confirmDelete = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = MinTouchTarget),
        ) {
            Text(stringResource(R.string.segment_delete), color = MaterialTheme.colorScheme.error)
        }

        val selected = ready.addresses.firstOrNull { it.id == selectedId }
        if (selected != null) {
            NumberMenuSheet(
                address = selected,
                onDismiss = { selectedId = null },
                onSetExists = {
                    viewModel.setExists(selected, it)
                    selectedId = null
                },
                onDelete = {
                    viewModel.delete(selected)
                    selectedId = null
                },
            )
        }
        if (adding) {
            AddNumberSheet(
                error = addError,
                onDismiss = { adding = false },
                onAdd = viewModel::addNumber,
            )
        }
        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text(stringResource(R.string.segment_delete_title, segment.streetName)) },
                text = {
                    Text(pluralStringResource(R.plurals.segment_delete_text, ready.addresses.size, ready.addresses.size))
                },
                confirmButton = {
                    TextButton(onClick = {
                        confirmDelete = false
                        viewModel.deleteSection()
                    }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
                },
            )
        }
    }
}

@Composable
private fun NumberGrid(addresses: List<Address>, onTap: (Address) -> Unit, onAdd: () -> Unit) {
    val cells: List<Address?> = addresses + null // null = the "Add" tile
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.chunked(COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { address ->
                    Box(Modifier.weight(1f)) {
                        if (address == null) AddTile(onAdd) else NumberTile(address) { onTap(address) }
                    }
                }
                repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun NumberTile(address: Address, onClick: () -> Unit) {
    val gone = !address.exists
    val style = KrantenwijkTheme.colors.doesNotExist
    val description = if (gone) {
        stringResource(R.string.number_tile_gone_description, address.label())
    } else {
        stringResource(R.string.number_tile_description, address.label())
    }
    val shape = RoundedCornerShape(14.dp)
    Surface(
        shape = shape,
        color = if (gone) KrantenwijkTheme.colors.page else MaterialTheme.colorScheme.surfaceContainer,
        border = if (gone) null else BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .then(if (gone) Modifier.dashedBorder(style.border) else Modifier)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = address.label(),
                style = MaterialTheme.typography.headlineMedium,
                color = if (gone) style.content else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (gone) TextDecoration.LineThrough else null,
            )
            if (gone) {
                Text(
                    text = stringResource(R.string.does_not_exist),
                    style = MaterialTheme.typography.labelSmall,
                    color = style.subContent,
                )
            }
        }
    }
}

@Composable
private fun AddTile(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth().height(72.dp).clickable(onClick = onClick),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.number_add), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberMenuSheet(
    address: Address,
    onDismiss: () -> Unit,
    onSetExists: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.number_sheet_title, address.label()), style = MaterialTheme.typography.headlineMedium)
            if (address.exists) {
                SheetOption(
                    title = stringResource(R.string.number_mark_gone),
                    subtitle = stringResource(R.string.number_mark_gone_sub),
                    onClick = { onSetExists(false) },
                )
            } else {
                SheetOption(
                    title = stringResource(R.string.number_mark_exists),
                    subtitle = stringResource(R.string.number_mark_exists_sub),
                    onClick = { onSetExists(true) },
                )
            }
            SheetOption(
                title = stringResource(R.string.number_delete),
                subtitle = stringResource(R.string.number_delete_sub),
                onClick = onDelete,
                destructive = true,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SheetOption(title: String, subtitle: String, onClick: () -> Unit, destructive: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).clickable(onClick = onClick).semantics { role = Role.Button },
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNumberSheet(error: AddNumberError?, onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var number by rememberSaveable { mutableStateOf("") }
    var addition by rememberSaveable { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.number_add_title), style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { v -> number = v.filter(Char::isDigit).take(4) },
                    label = { Text(stringResource(R.string.number_house_number)) },
                    placeholder = { Text("14") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = addition,
                    onValueChange = { v -> addition = v.take(6) },
                    label = { Text(stringResource(R.string.number_addition)) },
                    placeholder = { Text("A") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                    modifier = Modifier.weight(1f),
                )
            }
            when (error) {
                AddNumberError.InvalidNumber -> ErrorText(stringResource(R.string.number_add_invalid))
                is AddNumberError.Duplicate -> ErrorText(stringResource(R.string.number_add_duplicate, error.label))
                null -> Unit
            }
            AccentButton(
                text = stringResource(R.string.number_add),
                onClick = { onAdd(number, addition) },
                enabled = number.isNotEmpty(),
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
}
