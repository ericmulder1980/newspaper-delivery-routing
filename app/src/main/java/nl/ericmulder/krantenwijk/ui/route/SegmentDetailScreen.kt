package nl.ericmulder.krantenwijk.ui.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
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
import nl.ericmulder.krantenwijk.domain.model.DeliveryKind
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.rules.deliveryKind
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.DeliveryIcons
import nl.ericmulder.krantenwijk.ui.common.MinTouchTarget
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton
import nl.ericmulder.krantenwijk.ui.common.dashedBorder
import nl.ericmulder.krantenwijk.ui.common.label
import nl.ericmulder.krantenwijk.ui.common.shortLabel
import nl.ericmulder.krantenwijk.ui.common.style

private const val COLUMNS = 4

private fun Address.label() = "$houseNumber${addition.orEmpty()}"

/** What the sticker sheet acts on: one tapped number, or the current selection. */
private sealed interface SheetTarget {
    data class One(val addressId: Long) : SheetTarget

    data object Selection : SheetTarget
}

/** One street section: numbers and stickers (ADR-12, ADR-03, ADR-04, STK-01..03). */
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

    var sheetAddressId by rememberSaveable { mutableStateOf<Long?>(null) }
    var sheetForSelection by rememberSaveable { mutableStateOf(false) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var confirmDeleteSection by rememberSaveable { mutableStateOf(false) }
    var confirmDeleteIds by rememberSaveable { mutableStateOf<List<Long>?>(null) }

    LaunchedEffect(state) { if (state == SegmentDetailUiState.Gone) onBack() }
    LaunchedEffect(added) { if (added > 0) adding = false }

    val ready = state as? SegmentDetailUiState.Ready
    ScreenScaffold(
        title = ready?.segment?.streetName ?: "",
        onBack = if (ready?.selecting == true) viewModel::stopSelecting else onBack,
    ) {
        if (ready == null) return@ScreenScaffold
        Header(ready)
        Legend()
        SelectionBar(ready, viewModel)
        NumberGrid(
            ready = ready,
            onTap = { address ->
                if (ready.selecting) viewModel.toggleSelected(address) else sheetAddressId = address.id
            },
            onAdd = {
                viewModel.clearAddError()
                adding = true
            },
        )
        val selected = ready.selection
        if (selected != null) {
            AccentButton(
                text = stringResource(R.string.set_sticker_count, selected.size),
                onClick = { sheetForSelection = true },
                enabled = selected.isNotEmpty(),
            )
        } else {
            AccentButton(text = stringResource(R.string.segment_done), onClick = onBack)
            TextButton(
                onClick = { confirmDeleteSection = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = MinTouchTarget),
            ) {
                Text(stringResource(R.string.segment_delete), color = MaterialTheme.colorScheme.error)
            }
        }

        val target: SheetTarget? = when {
            sheetForSelection && !selected.isNullOrEmpty() -> SheetTarget.Selection
            sheetAddressId != null -> SheetTarget.One(sheetAddressId!!)
            else -> null
        }
        val targetAddresses = when (target) {
            is SheetTarget.One -> ready.addresses.filter { it.id == target.addressId }
            SheetTarget.Selection -> ready.addresses.filter { it.id in selected.orEmpty() }
            null -> emptyList()
        }
        if (targetAddresses.isNotEmpty()) {
            val close = {
                sheetAddressId = null
                sheetForSelection = false
            }
            StickerSheet(
                title = if (target is SheetTarget.One) {
                    stringResource(R.string.number_sheet_title, targetAddresses.single().label())
                } else {
                    pluralStringResource(R.plurals.numbers_selected, targetAddresses.size, targetAddresses.size)
                },
                current = targetAddresses.map { it.currentOption() }.distinct().singleOrNull(),
                deleteCount = targetAddresses.size,
                onPick = { option ->
                    viewModel.apply(option, targetAddresses.map { it.id })
                    close()
                },
                onDelete = {
                    val ids = targetAddresses.map { it.id }
                    close()
                    if (ids.size == 1) viewModel.delete(ids) else confirmDeleteIds = ids
                },
                onDismiss = close,
            )
        }
        if (adding) {
            AddNumberSheet(error = addError, onDismiss = { adding = false }, onAdd = viewModel::addNumber)
        }
        confirmDeleteIds?.let { ids ->
            ConfirmDialog(
                title = pluralStringResource(R.plurals.delete_numbers_title, ids.size, ids.size),
                text = stringResource(R.string.cannot_undo),
                onConfirm = {
                    confirmDeleteIds = null
                    viewModel.delete(ids)
                },
                onDismiss = { confirmDeleteIds = null },
            )
        }
        if (confirmDeleteSection) {
            ConfirmDialog(
                title = stringResource(R.string.segment_delete_title, ready.segment.streetName),
                text = pluralStringResource(R.plurals.segment_delete_text, ready.addresses.size, ready.addresses.size),
                onConfirm = {
                    confirmDeleteSection = false
                    viewModel.deleteSection()
                },
                onDismiss = { confirmDeleteSection = false },
            )
        }
    }
}

@Composable
private fun Header(ready: SegmentDetailUiState.Ready) {
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
    Text(stringResource(segment.side.label()) + " · " + range, style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), itemVerticalAlignment = Alignment.CenterVertically) {
        Text(
            pluralStringResource(R.plurals.address_count, ready.existingCount, ready.existingCount),
            style = MaterialTheme.typography.titleMedium,
        )
        CountWithIcon(R.drawable.ic_newspaper, pluralStringResource(R.plurals.newspaper_count, ready.counts.newspapers, ready.counts.newspapers))
        CountWithIcon(R.drawable.ic_leaflets, pluralStringResource(R.plurals.leaflet_count, ready.counts.leaflets, ready.counts.leaflets))
    }
    Text(
        text = stringResource(if (ready.selecting) R.string.segment_select_hint else R.string.segment_tap_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CountWithIcon(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Colour key, as in the prototype; each entry also shows the icons so colour is never the only cue. */
@Composable
private fun Legend() {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(DeliveryKind.BOTH, DeliveryKind.NEWSPAPER_ONLY, DeliveryKind.NOTHING, DeliveryKind.DOES_NOT_EXIST).forEach { kind ->
            val style = kind.style()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier
                        .size(16.dp)
                        .then(
                            if (style.dashedBorder) {
                                Modifier.dashedBorder(style.border, 1.5.dp, 4.dp)
                            } else {
                                Modifier.border(2.dp, style.border, RoundedCornerShape(4.dp))
                            },
                        )
                        .background(style.fill ?: androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(4.dp)),
                )
                Text(stringResource(kind.label()), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SelectionBar(ready: SegmentDetailUiState.Ready, viewModel: SegmentDetailViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (ready.selecting) {
            SecondaryButton(stringResource(R.string.select_all), viewModel::selectAll, Modifier.weight(1f))
            SecondaryButton(stringResource(R.string.cancel), viewModel::stopSelecting, Modifier.weight(1f))
        } else {
            SecondaryButton(stringResource(R.string.select), viewModel::startSelecting, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NumberGrid(ready: SegmentDetailUiState.Ready, onTap: (Address) -> Unit, onAdd: () -> Unit) {
    // null = the "Add" tile; hidden while selecting.
    val cells: List<Address?> = if (ready.selecting) ready.addresses else ready.addresses + null
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.chunked(COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { address ->
                    Box(Modifier.weight(1f)) {
                        if (address == null) {
                            AddTile(onAdd)
                        } else {
                            StickerTile(
                                address = address,
                                selected = ready.selection?.contains(address.id),
                                onClick = { onTap(address) },
                            )
                        }
                    }
                }
                repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * A number coloured by what it receives in a full round, with icons and the sticker as text (STK-03).
 * [selected] is null outside selection mode.
 */
@Composable
private fun StickerTile(address: Address, selected: Boolean?, onClick: () -> Unit) {
    val kind = deliveryKind(address)
    val style = kind.style()
    val gone = kind == DeliveryKind.DOES_NOT_EXIST
    val outlineOnly = style.fill == null
    val description = stringResource(
        R.string.tile_description,
        address.label(),
        if (gone) stringResource(R.string.does_not_exist) else stringResource(address.sticker.label()),
        stringResource(kind.label()),
    )
    val shape = RoundedCornerShape(14.dp)
    val selectionBorder = if (selected == true) BorderStroke(4.dp, MaterialTheme.colorScheme.onSurface) else null
    Surface(
        shape = shape,
        color = style.fill ?: MaterialTheme.colorScheme.surface,
        contentColor = style.content,
        border = selectionBorder ?: if (style.dashedBorder) null else BorderStroke(2.dp, style.border),
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .then(if (style.dashedBorder && selected != true) Modifier.dashedBorder(style.border) else Modifier)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = if (selected != null) Role.Checkbox else Role.Button
                if (selected != null) this.selected = selected
            },
    ) {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            ) {
                Text(
                    text = address.label(),
                    style = MaterialTheme.typography.headlineMedium,
                    textDecoration = if (gone) TextDecoration.LineThrough else null,
                )
                DeliveryIcons(kind, tint = style.content, size = 18.dp)
                Text(
                    text = if (gone) stringResource(R.string.does_not_exist) else stringResource(address.sticker.shortLabel()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (outlineOnly) FontWeight.Normal else FontWeight.Bold,
                    color = style.subContent,
                )
            }
            if (selected == true) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.onSurface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_check), null, Modifier.size(16.dp), MaterialTheme.colorScheme.surface)
                }
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
        modifier = Modifier.fillMaxWidth().height(84.dp).clickable(onClick = onClick),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.number_add), style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** "Sticker on mailbox": the four stickers plus "does not exist" (as in the prototype), and delete. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StickerSheet(
    title: String,
    current: StickerOption?,
    deleteCount: Int,
    onPick: (StickerOption) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.sticker_sheet_kicker),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(title, style = MaterialTheme.typography.headlineMedium)
            StickerOption.all.forEach { option -> StickerOptionRow(option, isCurrent = option == current) { onPick(option) } }
            TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().heightIn(min = MinTouchTarget)) {
                Text(
                    pluralStringResource(R.plurals.delete_numbers_action, deleteCount, deleteCount),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StickerOptionRow(option: StickerOption, isCurrent: Boolean, onClick: () -> Unit) {
    val kind = when (option) {
        is StickerOption.Set -> deliveryKind(Address(houseNumber = 1, sticker = option.sticker))
        StickerOption.DoesNotExist -> DeliveryKind.DOES_NOT_EXIST
    }
    val style = kind.style()
    val title = when (option) {
        is StickerOption.Set -> stringResource(option.sticker.label())
        StickerOption.DoesNotExist -> stringResource(R.string.does_not_exist)
    }
    val result = if (option == StickerOption.DoesNotExist) stringResource(R.string.delivery_skip) else stringResource(kind.label())
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = style.fill ?: MaterialTheme.colorScheme.surface,
        contentColor = style.content,
        border = if (style.dashedBorder) null else BorderStroke(2.dp, if (isCurrent) MaterialTheme.colorScheme.onSurface else style.border),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(if (style.dashedBorder) Modifier.dashedBorder(style.border) else Modifier)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                role = Role.RadioButton
                selected = isCurrent
            },
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(result, style = MaterialTheme.typography.bodyMedium, color = style.subContent)
            }
            DeliveryIcons(kind, tint = style.content)
            if (isCurrent) {
                Text(
                    stringResource(R.string.sticker_current),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
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
            AccentButton(text = stringResource(R.string.number_add), onClick = { onAdd(number, addition) }, enabled = number.isNotEmpty())
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ConfirmDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
}
