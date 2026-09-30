package nl.ericmulder.krantenwijk.ui.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Building
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.domain.model.unitLabel
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton

/** Five columns so 26 mailboxes (A–Z) fit one portrait screen (BLD-03). */
private const val BUILDING_COLUMNS = 5

/** Apartment building zoomed in: the bank of mailboxes (BLD-03..05). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingDetailScreen(
    buildingId: Long,
    onBack: () -> Unit,
    viewModel: BuildingDetailViewModel = hiltViewModel<BuildingDetailViewModel, BuildingDetailViewModel.Factory>(
        key = "building-$buildingId",
        creationCallback = { it.create(buildingId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val addError by viewModel.addError.collectAsStateWithLifecycle()
    val added by viewModel.added.collectAsStateWithLifecycle()

    var sheetApartmentId by rememberSaveable { mutableStateOf<Long?>(null) }
    var sheetForSelection by rememberSaveable { mutableStateOf(false) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var confirmDeleteIds by rememberSaveable { mutableStateOf<List<Long>?>(null) }

    LaunchedEffect(state) { if (state == BuildingDetailUiState.Gone) onBack() }
    LaunchedEffect(added) { if (added > 0) adding = false }

    val ready = state as? BuildingDetailUiState.Ready
    val building = ready?.building
    ScreenScaffold(
        title = building?.let { stringResource(R.string.number_sheet_title, it.houseNumber.toString()) } ?: "",
        onBack = if (ready?.selecting == true) viewModel::stopSelecting else onBack,
    ) {
        if (ready == null || building == null) return@ScreenScaffold
        Text(
            text = stringResource(R.string.building_kicker, ready.streetName, building.houseNumber),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), itemVerticalAlignment = Alignment.CenterVertically) {
            Text(
                pluralStringResource(R.plurals.apartment_count, ready.existingCount, ready.existingCount),
                style = MaterialTheme.typography.titleMedium,
            )
            CountWithIcon(R.drawable.ic_newspaper, pluralStringResource(R.plurals.newspaper_count, ready.counts.newspapers, ready.counts.newspapers))
            CountWithIcon(R.drawable.ic_leaflets, pluralStringResource(R.plurals.leaflet_count, ready.counts.leaflets, ready.counts.leaflets))
        }
        Text(
            text = stringResource(if (ready.selecting) R.string.building_select_hint else R.string.building_tap_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (ready.selecting) {
                SecondaryButton(stringResource(R.string.select_all), viewModel::selectAll, Modifier.weight(1f))
                SecondaryButton(stringResource(R.string.cancel), viewModel::stopSelecting, Modifier.weight(1f))
            } else {
                SecondaryButton(stringResource(R.string.select), viewModel::startSelecting, Modifier.weight(1f))
            }
        }
        MailboxGrid(
            ready = ready,
            onTap = { apartment ->
                if (ready.selecting) viewModel.toggleSelected(apartment) else sheetApartmentId = apartment.id
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
            AccentButton(text = stringResource(R.string.building_done, ready.streetName), onClick = onBack)
        }

        val targets = when {
            sheetForSelection && !selected.isNullOrEmpty() -> ready.apartments.filter { it.id in selected }
            sheetApartmentId != null -> ready.apartments.filter { it.id == sheetApartmentId }
            else -> emptyList()
        }
        if (targets.isNotEmpty()) {
            val close = {
                sheetApartmentId = null
                sheetForSelection = false
            }
            StickerSheet(
                title = if (targets.size == 1 && !sheetForSelection) {
                    stringResource(R.string.number_sheet_title, building.unitLabel(targets.single().addition.orEmpty()))
                } else {
                    pluralStringResource(R.plurals.apartments_selected, targets.size, targets.size)
                },
                current = targets.map { it.currentOption() }.distinct().singleOrNull(),
                deleteCount = targets.size,
                onMakeBuilding = null,
                onPick = { option ->
                    viewModel.apply(option, targets.map { it.id })
                    close()
                },
                onDelete = {
                    val ids = targets.map { it.id }
                    close()
                    if (ids.size == 1) viewModel.delete(ids) else confirmDeleteIds = ids
                },
                onDismiss = close,
            )
        }
        if (adding) {
            AddApartmentSheet(
                building = building,
                error = addError,
                onAdd = viewModel::addApartment,
                onDismiss = { adding = false },
            )
        }
        confirmDeleteIds?.let { ids ->
            ConfirmDialog(
                title = pluralStringResource(R.plurals.delete_apartments_title, ids.size, ids.size),
                text = stringResource(R.string.cannot_undo),
                onConfirm = {
                    confirmDeleteIds = null
                    viewModel.delete(ids)
                },
                onDismiss = { confirmDeleteIds = null },
            )
        }
    }
}

@Composable
private fun MailboxGrid(ready: BuildingDetailUiState.Ready, onTap: (Address) -> Unit, onAdd: () -> Unit) {
    val cells = if (ready.selecting) ready.apartments else ready.apartments + null // null = "Add" tile
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        cells.chunked(BUILDING_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { apartment ->
                    Box(Modifier.weight(1f)) {
                        if (apartment == null) {
                            AddTile(onAdd, compact = true)
                        } else {
                            val suffix = apartment.addition.orEmpty()
                            StickerTile(
                                address = apartment,
                                label = if (ready.building.suffixType == SuffixType.NUMBER) "${ready.building.separator}$suffix" else suffix,
                                spokenLabel = ready.building.unitLabel(suffix),
                                selected = ready.selection?.contains(apartment.id),
                                onClick = { onTap(apartment) },
                                compact = true,
                            )
                        }
                    }
                }
                repeat(BUILDING_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddApartmentSheet(building: Building, error: AddApartmentError?, onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var value by rememberSaveable { mutableStateOf("") }
    val letters = building.suffixType == SuffixType.LETTER
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.apartment_add_title, building.houseNumber), style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = value,
                onValueChange = { v -> value = if (letters) v.uppercase().take(1) else v.filter(Char::isDigit).take(3) },
                label = { Text(stringResource(if (letters) R.string.apartment_letter else R.string.apartment_number)) },
                placeholder = { Text(if (letters) "M" else "21") },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium,
                keyboardOptions = if (letters) {
                    KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done)
                } else {
                    KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (value.isNotEmpty()) {
                Text(
                    stringResource(R.string.apartment_add_preview, building.unitLabel(value)),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            when (error) {
                AddApartmentError.Invalid -> ErrorText(
                    stringResource(if (letters) R.string.apartment_invalid_letter else R.string.apartment_invalid_number),
                )
                is AddApartmentError.Duplicate -> ErrorText(stringResource(R.string.apartment_duplicate, error.label))
                null -> Unit
            }
            AccentButton(text = stringResource(R.string.number_add), onClick = { onAdd(value) }, enabled = value.isNotEmpty())
            Spacer(Modifier.height(12.dp))
        }
    }
}
