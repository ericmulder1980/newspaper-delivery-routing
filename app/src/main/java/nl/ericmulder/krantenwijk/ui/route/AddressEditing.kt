package nl.ericmulder.krantenwijk.ui.route

import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
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
import nl.ericmulder.krantenwijk.ui.common.PrimaryActionHeight
import nl.ericmulder.krantenwijk.ui.common.TargetSpacing
import nl.ericmulder.krantenwijk.ui.common.WalkTouchTarget
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton
import nl.ericmulder.krantenwijk.ui.common.dashedBorder
import nl.ericmulder.krantenwijk.ui.common.label
import nl.ericmulder.krantenwijk.ui.common.shortLabel
import nl.ericmulder.krantenwijk.ui.common.style
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.domain.model.unitLabel
import nl.ericmulder.krantenwijk.domain.rules.separatorFor
import nl.ericmulder.krantenwijk.domain.rules.unitsOrNull
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme

// Tiles, grids, sheets and dialogs shared by the street, building and round screens.

/** Columns in a street's number grid. */
private const val COLUMNS = 4

@Composable
internal fun CountWithIcon(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * A number coloured by what it receives in a full round, with icons and the sticker as text (STK-03).
 * [selected] is null outside selection mode.
 */
@Composable
internal fun StickerTile(
    address: Address,
    label: String,
    spokenLabel: String,
    selected: Boolean?,
    onClick: () -> Unit,
    compact: Boolean = false,
) {
    val kind = deliveryKind(address)
    val style = kind.style()
    val gone = kind == DeliveryKind.DOES_NOT_EXIST
    val outlineOnly = style.fill == null
    val description = stringResource(
        R.string.tile_description,
        spokenLabel,
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
            .height(if (compact) PrimaryActionHeight else 84.dp)
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
                modifier = Modifier.fillMaxWidth().padding(vertical = if (compact) 2.dp else 6.dp),
            ) {
                Text(
                    text = label,
                    style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                    textDecoration = if (gone) TextDecoration.LineThrough else null,
                )
                DeliveryIcons(kind, tint = style.content, size = if (compact) 16.dp else 18.dp)
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
internal fun AddTile(onClick: () -> Unit, compact: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth().height(if (compact) PrimaryActionHeight else 84.dp).clickable(onClick = onClick),
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
internal fun StickerSheet(
    title: String,
    current: StickerOption?,
    deleteCount: Int,
    onMakeBuilding: (() -> Unit)?,
    onPick: (StickerOption) -> Unit,
    onDelete: (() -> Unit)?,
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
            if (onMakeBuilding != null) {
                SheetAction(
                    title = stringResource(R.string.building_make),
                    subtitle = stringResource(R.string.building_make_sub),
                    onClick = onMakeBuilding,
                )
            }
            if (onDelete != null) {
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().heightIn(min = MinTouchTarget)) {
                    Text(
                        pluralStringResource(R.plurals.delete_numbers_action, deleteCount, deleteCount),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
internal fun StickerOptionRow(option: StickerOption, isCurrent: Boolean, onClick: () -> Unit) {
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
            .heightIn(min = WalkTouchTarget)
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

@Composable
internal fun ConfirmDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = stringResource(R.string.delete),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel, color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
internal fun ErrorText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
}

@Composable
internal fun SheetAction(title: String, subtitle: String, onClick: () -> Unit, destructive: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().heightIn(min = WalkTouchTarget).clickable(onClick = onClick).semantics(mergeDescendants = true) {
            role = Role.Button
        },
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** "12" or "14A": a standalone house number with its addition. */
internal fun Address.houseLabel() = "$houseNumber${addition.orEmpty()}"

/** Colour key, as in the prototype; each entry also shows the icons so colour is never the only cue. */
@Composable
internal fun Legend() {
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
internal fun NumberGrid(
    cells: List<SegmentCell>,
    selection: Set<Long>?,
    onTap: (Address) -> Unit,
    onTapBuilding: (BuildingSummary) -> Unit,
    onAdd: (() -> Unit)?,
) {
    // Houses fill rows of four; a building takes a full row at the position of its number.
    val rows = buildList<List<SegmentCell?>> {
        var current = mutableListOf<SegmentCell?>()
        fun flush() {
            if (current.isNotEmpty()) add(current)
            current = mutableListOf()
        }
        val withAdd: List<SegmentCell?> = if (onAdd == null) cells else cells + null // null = "Add" tile
        withAdd.forEach { cell ->
            if (cell is SegmentCell.Apartments) {
                flush()
                add(listOf(cell))
            } else {
                current += cell
                if (current.size == COLUMNS) flush()
            }
        }
        flush()
    }
    Column(verticalArrangement = Arrangement.spacedBy(TargetSpacing)) {
        rows.forEach { row ->
            val only = row.singleOrNull()
            if (only is SegmentCell.Apartments) {
                BuildingRow(only.summary, dimmed = selection != null) { onTapBuilding(only.summary) }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(TargetSpacing)) {
                    row.forEach { cell ->
                        Box(Modifier.weight(1f)) {
                            when (cell) {
                                null -> AddTile(onAdd ?: {})
                                is SegmentCell.House -> StickerTile(
                                    address = cell.address,
                                    label = cell.address.houseLabel(),
                                    spokenLabel = cell.address.houseLabel(),
                                    selected = selection?.contains(cell.address.id),
                                    onClick = { onTap(cell.address) },
                                )
                                is SegmentCell.Apartments -> Unit
                            }
                        }
                    }
                    repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** A building as one row (BLD-02): number, apartment count and range, sticker summary, colour strip, counts. */
@Composable
internal fun BuildingRow(summary: BuildingSummary, dimmed: Boolean, onClick: () -> Unit) {
    val building = summary.building
    val first = summary.apartments.firstOrNull()?.addition
    val last = summary.apartments.lastOrNull()?.addition
    val range = if (first != null && last != null) "${building.unitLabel(first)}–${building.unitLabel(last)}" else ""
    val stickers = stickerSummaryText(summary)
    val description = stringResource(
        R.string.building_row_description,
        building.houseNumber,
        pluralStringResource(R.plurals.apartment_count, summary.existingCount, summary.existingCount),
        stickers.ifEmpty { stringResource(R.string.sticker_none) },
    )
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clickable(enabled = !dimmed, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            },
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(building.houseNumber.toString(), style = MaterialTheme.typography.displaySmall)
                Column(Modifier.weight(1f)) {
                    Text(
                        pluralStringResource(R.plurals.apartment_count, summary.existingCount, summary.existingCount) +
                            if (range.isNotEmpty()) " · $range" else "",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (stickers.isNotEmpty()) {
                        Text(stickers, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (!dimmed) {
                    Text(stringResource(R.string.building_zoom_in), style = MaterialTheme.typography.labelLarge)
                }
            }
            UnitStrip(summary.apartments)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CountWithIcon(R.drawable.ic_newspaper, pluralStringResource(R.plurals.newspaper_count, summary.counts.newspapers, summary.counts.newspapers))
                CountWithIcon(R.drawable.ic_leaflets, pluralStringResource(R.plurals.leaflet_count, summary.counts.leaflets, summary.counts.leaflets))
            }
        }
    }
}

/** "3 NEE/JA · 2 NEE/NEE": only stickers that block something are listed, as in plan BLD-02. */
@Composable
internal fun stickerSummaryText(summary: BuildingSummary): String = listOfNotNull(
    summary.stickers.neeJa.takeIf { it > 0 }?.let { "$it ${stringResource(R.string.sticker_short_nee_ja)}" },
    summary.stickers.neeNee.takeIf { it > 0 }?.let { "$it ${stringResource(R.string.sticker_short_nee_nee)}" },
).joinToString(" · ")

/** One small block per apartment in its delivery colour, like the prototype's strip. */
@Composable
internal fun UnitStrip(apartments: List<Address>) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
        apartments.forEach { apartment ->
            val style = deliveryKind(apartment).style()
            Box(
                Modifier
                    .weight(1f)
                    .height(10.dp)
                    .then(
                        if (style.dashedBorder) {
                            Modifier.dashedBorder(style.border, 1.dp, 2.dp)
                        } else {
                            Modifier.border(1.dp, style.border, RoundedCornerShape(2.dp))
                        },
                    )
                    .background(style.fill ?: androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(2.dp)),
            )
        }
    }
}
