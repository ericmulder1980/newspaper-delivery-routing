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
import nl.ericmulder.krantenwijk.ui.common.WizardProgress
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton
import nl.ericmulder.krantenwijk.ui.common.dashedBorder
import nl.ericmulder.krantenwijk.ui.common.label
import nl.ericmulder.krantenwijk.ui.common.shortLabel
import nl.ericmulder.krantenwijk.ui.common.style
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.domain.model.unitLabel
import nl.ericmulder.krantenwijk.domain.rules.separatorFor
import nl.ericmulder.krantenwijk.domain.rules.unitsOrNull
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme



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
    onOpenBuilding: (buildingId: Long) -> Unit,
    /** "Done": back to the route, or to the wizard's walking route step. */
    onDone: () -> Unit = onBack,
    wizardStep: Int? = null,
    viewModel: SegmentDetailViewModel = hiltViewModel<SegmentDetailViewModel, SegmentDetailViewModel.Factory>(
        key = "segment-$segmentId",
        creationCallback = { it.create(segmentId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val addError by viewModel.addError.collectAsStateWithLifecycle()
    val added by viewModel.added.collectAsStateWithLifecycle()
    val buildingError by viewModel.buildingError.collectAsStateWithLifecycle()
    val buildingCreated by viewModel.buildingCreated.collectAsStateWithLifecycle()

    var sheetAddressId by rememberSaveable { mutableStateOf<Long?>(null) }
    var sheetForSelection by rememberSaveable { mutableStateOf(false) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var confirmDeleteSection by rememberSaveable { mutableStateOf(false) }
    var confirmDeleteIds by rememberSaveable { mutableStateOf<List<Long>?>(null) }
    var buildingForAddressId by rememberSaveable { mutableStateOf<Long?>(null) }
    var buildingSheetId by rememberSaveable { mutableStateOf<Long?>(null) }
    var confirmRemoveBuildingId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(state) { if (state == SegmentDetailUiState.Gone) onBack() }
    LaunchedEffect(added) { if (added > 0) adding = false }
    LaunchedEffect(buildingCreated) { if (buildingCreated > 0) buildingForAddressId = null }

    val ready = state as? SegmentDetailUiState.Ready
    ScreenScaffold(
        title = ready?.segment?.streetName ?: "",
        onBack = if (ready?.selecting == true) viewModel::stopSelecting else onBack,
    ) {
        if (ready == null) return@ScreenScaffold
        wizardStep?.let { WizardProgress(it) }
        Header(ready)
        if (!ready.selecting) {
            ReverseToggle(
                reverse = ready.segment.direction == Direction.DESCENDING,
                onChange = viewModel::setReverse,
            )
        }
        Legend()
        SelectionBar(ready, viewModel)
        NumberGrid(
            cells = ready.cells,
            selection = ready.selection,
            onTap = { address ->
                if (ready.selecting) viewModel.toggleSelected(address) else sheetAddressId = address.id
            },
            onTapBuilding = { summary -> if (!ready.selecting) buildingSheetId = summary.building.id },
            onAdd = if (ready.selecting) {
                null
            } else {
                {
                    viewModel.clearAddError()
                    adding = true
                }
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
            AccentButton(
                text = stringResource(if (wizardStep != null) R.string.onboarding_street_done else R.string.segment_done),
                onClick = onDone,
            )
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
                    stringResource(R.string.number_sheet_title, targetAddresses.single().houseLabel())
                } else {
                    pluralStringResource(R.plurals.numbers_selected, targetAddresses.size, targetAddresses.size)
                },
                current = targetAddresses.map { it.currentOption() }.distinct().singleOrNull(),
                deleteCount = targetAddresses.size,
                onMakeBuilding = if (target is SheetTarget.One) {
                    {
                        val id = targetAddresses.single().id
                        close()
                        viewModel.clearBuildingError()
                        buildingForAddressId = id
                    }
                } else {
                    null
                },
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
        ready.addresses.firstOrNull { it.id == buildingForAddressId }?.let { address ->
            CreateBuildingSheet(
                houseNumber = address.houseNumber,
                error = buildingError,
                onCreate = { type, from, to -> viewModel.createBuilding(address, type, from, to) },
                onDismiss = { buildingForAddressId = null },
            )
        }
        ready.cells.firstNotNullOfOrNull { (it as? SegmentCell.Apartments)?.summary?.takeIf { s -> s.building.id == buildingSheetId } }
            ?.let { summary ->
                BuildingSheet(
                    summary = summary,
                    onZoomIn = {
                        buildingSheetId = null
                        onOpenBuilding(summary.building.id)
                    },
                    onRemove = {
                        buildingSheetId = null
                        confirmRemoveBuildingId = summary.building.id
                    },
                    onDismiss = { buildingSheetId = null },
                )
            }
        confirmRemoveBuildingId?.let { id ->
            val summary = ready.cells.firstNotNullOfOrNull { (it as? SegmentCell.Apartments)?.summary?.takeIf { s -> s.building.id == id } }
            if (summary != null) {
                ConfirmDialog(
                    title = stringResource(R.string.building_remove_title, summary.building.houseNumber),
                    text = pluralStringResource(R.plurals.building_remove_text, summary.apartments.size, summary.apartments.size),
                    onConfirm = {
                        confirmRemoveBuildingId = null
                        viewModel.removeBuilding(id)
                    },
                    onDismiss = { confirmRemoveBuildingId = null },
                    confirmLabel = stringResource(R.string.building_remove_confirm),
                )
            }
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

/** Tap on a building row: zoom in to the apartments (BLD-B) or turn it back into one number. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuildingSheet(summary: BuildingSummary, onZoomIn: () -> Unit, onRemove: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.number_sheet_title, summary.building.houseNumber.toString()) + " · " +
                    pluralStringResource(R.plurals.apartment_count, summary.existingCount, summary.existingCount),
                style = MaterialTheme.typography.headlineMedium,
            )
            SheetAction(stringResource(R.string.building_zoom_in), stringResource(R.string.building_zoom_in_sub), onZoomIn)
            SheetAction(
                stringResource(R.string.building_remove),
                stringResource(R.string.building_remove_sub),
                onRemove,
                destructive = true,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** "Apartment building" for one number: letters or numbers, first and last, live preview (BLD-01). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateBuildingSheet(
    houseNumber: Int,
    error: BuildingError?,
    onCreate: (SuffixType, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var type by rememberSaveable { mutableStateOf(SuffixType.LETTER) }
    var from by rememberSaveable { mutableStateOf("A") }
    var to by rememberSaveable { mutableStateOf("L") }
    val units = unitsOrNull(from, to, type)
    val separator = separatorFor(type)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.building_sheet_title, houseNumber), style = MaterialTheme.typography.headlineMedium)
            Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(SuffixType.LETTER to R.string.building_type_letters, SuffixType.NUMBER to R.string.building_type_numbers)
                    .forEach { (option, label) ->
                        ToggleChoice(
                            text = stringResource(label),
                            selected = type == option,
                            onClick = {
                                type = option
                                if (option == SuffixType.LETTER) {
                                    from = "A"
                                    to = "L"
                                } else {
                                    from = "1"
                                    to = "20"
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val maxLength = if (type == SuffixType.LETTER) 1 else 3
                val keyboard = if (type == SuffixType.LETTER) {
                    KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text)
                } else {
                    KeyboardOptions(keyboardType = KeyboardType.Number)
                }
                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it.uppercase().take(maxLength) },
                    label = { Text(stringResource(R.string.building_first)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    keyboardOptions = keyboard,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it.uppercase().take(maxLength) },
                    label = { Text(stringResource(R.string.building_last)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium,
                    keyboardOptions = keyboard,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = if (units != null) {
                    pluralStringResource(R.plurals.apartment_count, units.size, units.size) + ": " +
                        "$houseNumber$separator${units.first()} … $houseNumber$separator${units.last()}"
                } else {
                    stringResource(if (type == SuffixType.LETTER) R.string.building_invalid_letters else R.string.building_invalid_numbers)
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            if (error is BuildingError.Conflict) {
                ErrorText(stringResource(R.string.building_conflict, error.labels.joinToString(", ")))
            }
            AccentButton(
                text = stringResource(R.string.building_create),
                onClick = { onCreate(type, from, to) },
                enabled = units != null,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ToggleChoice(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = KrantenwijkTheme.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) colors.accentFill else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = if (selected) colors.onAccentFill else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(2.dp, if (selected) colors.accentFillBorder else MaterialTheme.colorScheme.outline),
        modifier = modifier
            .heightIn(min = MinTouchTarget)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)) {
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** "Walk in reverse order" for an existing section (ADR-06), same wording as when adding it. */
@Composable
private fun ReverseToggle(reverse: Boolean, onChange: (Boolean) -> Unit) {
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
