package nl.ericmulder.krantenwijk.ui.round

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.MinTouchTarget
import nl.ericmulder.krantenwijk.ui.common.PrimaryActionHeight
import nl.ericmulder.krantenwijk.ui.common.TargetSpacing
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton
import nl.ericmulder.krantenwijk.ui.common.label
import nl.ericmulder.krantenwijk.ui.route.CountWithIcon
import nl.ericmulder.krantenwijk.ui.route.Legend
import nl.ericmulder.krantenwijk.ui.route.NumberGrid
import nl.ericmulder.krantenwijk.ui.route.SegmentCell
import nl.ericmulder.krantenwijk.ui.route.StickerSheet
import nl.ericmulder.krantenwijk.ui.route.currentOption
import nl.ericmulder.krantenwijk.ui.route.houseLabel
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme

/** One street section while delivering, with Previous/Next street (RND-B, DEC-022). */
@Composable
fun RoundStreetScreen(
    segmentId: Long,
    onBack: () -> Unit,
    onGoTo: (segmentId: Long) -> Unit,
    /** Called after the round is saved, with its id (null if no round was active). */
    onFinish: (roundId: Long?) -> Unit,
    onOpenBuilding: (buildingId: Long) -> Unit,
    viewModel: RoundStreetViewModel = hiltViewModel<RoundStreetViewModel, RoundStreetViewModel.Factory>(
        key = "round-$segmentId",
        creationCallback = { it.create(segmentId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val roundActive by viewModel.roundActive.collectAsStateWithLifecycle()
    var sheetAddressId by rememberSaveable { mutableStateOf<Long?>(null) }
    var askAbandon by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state) { if (state == RoundStreetUiState.Gone) onBack() }

    // Leaving mid-round asks first (RND-13); Previous/Next and Finish don't.
    val leave = { if (roundActive) askAbandon = true else onBack() }
    BackHandler(enabled = roundActive) { askAbandon = true }
    if (askAbandon) {
        AbandonRoundDialog(
            onResume = { askAbandon = false },
            onAbandon = {
                askAbandon = false
                viewModel.abandonRound(then = onBack)
            },
        )
    }

    val ready = state as? RoundStreetUiState.Ready
    KeepScreenOn(ready?.keepScreenOn == true)
    ScreenScaffold(
        title = ready?.segment?.streetName ?: "",
        onBack = leave,
        bottomBar = { if (ready != null) PreviousNextBar(ready, onGoTo, onFinish = { viewModel.finishRound(then = onFinish) }) },
    ) {
        if (ready == null) return@ScreenScaffold
        val segment = ready.segment
        Text(
            stringResource(R.string.round_street_kicker, ready.position, ready.sectionCount),
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
            CountWithIcon(R.drawable.ic_newspaper, pluralStringResource(R.plurals.newspaper_count, ready.counts.newspapers, ready.counts.newspapers))
            CountWithIcon(R.drawable.ic_leaflets, pluralStringResource(R.plurals.leaflet_count, ready.counts.leaflets, ready.counts.leaflets))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchTarget)
                .toggleable(value = ready.showSkipped, role = Role.Switch, onValueChange = viewModel::setShowSkipped),
        ) {
            Text(stringResource(R.string.settings_show_skipped), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = ready.showSkipped, onCheckedChange = null)
        }
        if (ready.hiddenSkipped > 0) {
            Text(
                pluralStringResource(R.plurals.round_hidden_skipped, ready.hiddenSkipped, ready.hiddenSkipped),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Legend()
        NumberGrid(
            cells = ready.cells,
            selection = null,
            onTap = { sheetAddressId = it.id },
            onTapBuilding = { onOpenBuilding(it.building.id) },
            onAdd = null,
        )

        val address = ready.cells.firstNotNullOfOrNull {
            (it as? SegmentCell.House)?.address?.takeIf { a -> a.id == sheetAddressId }
        }
        if (address != null) {
            StickerSheet(
                title = stringResource(R.string.number_sheet_title, address.houseLabel()),
                current = address.currentOption(),
                deleteCount = 1,
                onMakeBuilding = null,
                onPick = { option ->
                    viewModel.apply(option, listOf(address.id))
                    sheetAddressId = null
                },
                onDelete = null,
                onDismiss = { sheetAddressId = null },
            )
        }
    }
}

/** "Abandon this round?" with Resume as the big, safe choice (RND-13, wireframe H). */
@Composable
private fun AbandonRoundDialog(onResume: () -> Unit, onAbandon: () -> Unit) {
    AlertDialog(
        onDismissRequest = onResume,
        title = { Text(stringResource(R.string.round_abandon_title)) },
        text = { Text(stringResource(R.string.round_abandon_message), style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(TargetSpacing)) {
                AccentButton(stringResource(R.string.round_resume), onResume)
                SecondaryButton(stringResource(R.string.round_abandon), onAbandon)
            }
        },
    )
}

/** Keeps the display on while this screen is shown (RND-08). */
@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

/** Big Previous/Next street buttons naming the adjacent sections; Next becomes "Finish round" at the end. */
@Composable
private fun PreviousNextBar(ready: RoundStreetUiState.Ready, onGoTo: (Long) -> Unit, onFinish: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 4.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(TargetSpacing),
        ) {
            val previous = ready.previous
            NavButton(
                label = stringResource(R.string.round_previous),
                detail = previous?.shortName() ?: stringResource(R.string.round_start_of_route),
                enabled = previous != null,
                accent = false,
                onClick = { previous?.let { onGoTo(it.id) } },
                modifier = Modifier.weight(1f),
            )
            val next = ready.next
            NavButton(
                label = stringResource(if (next != null) R.string.round_next else R.string.round_finish),
                detail = next?.shortName() ?: stringResource(R.string.round_end_of_route),
                enabled = true,
                accent = true,
                onClick = { if (next != null) onGoTo(next.id) else onFinish() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Segment.shortName(): String = "$streetName · ${stringResource(side.label())}"

@Composable
private fun NavButton(
    label: String,
    detail: String,
    enabled: Boolean,
    accent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.headlineSmall)
            Text(detail, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    val shape = RoundedCornerShape(16.dp)
    if (accent) {
        val colors = KrantenwijkTheme.colors
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            border = BorderStroke(2.dp, colors.accentFillBorder),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accentFill, contentColor = colors.onAccentFill),
            modifier = modifier.heightIn(min = PrimaryActionHeight),
        ) { content() }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            modifier = modifier.heightIn(min = PrimaryActionHeight),
        ) { content() }
    }
}
