package nl.ericmulder.krantenwijk.ui.besttimes

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import nl.ericmulder.krantenwijk.domain.rules.formatRoundTime
import nl.ericmulder.krantenwijk.domain.rules.roundSeconds
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.MinTouchTarget
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.WalkTouchTarget
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import java.text.SimpleDateFormat
import java.util.Date

/** Top 5 after a round (RND-14, wireframe J). Done and back both go home. */
@Composable
fun Top5Screen(
    roundId: Long,
    onDone: () -> Unit,
    viewModel: Top5ViewModel = hiltViewModel<Top5ViewModel, Top5ViewModel.Factory>(
        key = "top5-$roundId",
        creationCallback = { it.create(roundId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenScaffold(title = stringResource(R.string.top5_title), onBack = onDone) {
        val ready = state ?: return@ScreenScaffold
        Text(
            stringResource(R.string.top5_kicker),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ready.top.forEach { RoundRow(it, isNew = it.round.id == ready.newRoundId) }
        ready.newOutsideTop?.let { OutsideTop(it) }
        AccentButton(stringResource(R.string.top5_done), onDone)
    }
}

/** One place in the Top 5. A new round gets a thick accent border and a "New" badge with a star: never colour alone. */
@Composable
private fun RoundRow(ranked: RankedRound, isNew: Boolean) {
    val colors = KrantenwijkTheme.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = if (isNew) BorderStroke(3.dp, colors.accentFillBorder) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.heightIn(min = 84.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(3.dp, if (isNew) colors.accentFillBorder else MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("${ranked.rank}", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(formatRoundTime(roundSeconds(ranked.round.durationMillis)), style = MaterialTheme.typography.displaySmall)
                Text(
                    roundDate(ranked.round),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isNew) NewBadge()
        }
    }
}

@Composable
private fun NewBadge() {
    val colors = KrantenwijkTheme.colors
    Surface(shape = RoundedCornerShape(8.dp), color = colors.accentFill, contentColor = colors.onAccentFill) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(painterResource(R.drawable.ic_star), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(stringResource(R.string.top5_new).uppercase(), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** "Your time today 1:24:03 · Place 8" when the new round didn't make the top 5. */
@Composable
private fun OutsideTop(ranked: RankedRound) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.top5_your_time), style = MaterialTheme.typography.labelLarge)
                Text(formatRoundTime(roundSeconds(ranked.round.durationMillis)), style = MaterialTheme.typography.headlineMedium)
            }
            Text(stringResource(R.string.top5_place, ranked.rank), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

/**
 * Settings › Best times (RND-14, wireframe K): all finished rounds, fastest first. Hold a time to start
 * selecting, tap to add more, then delete; a message offers Undo.
 */
@Composable
fun BestTimesScreen(onBack: () -> Unit, viewModel: BestTimesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    val undoLabel = stringResource(R.string.best_times_undo)
    val selecting = state?.selecting == true
    BackHandler(enabled = selecting) { viewModel.clearSelection() }

    ScreenScaffold(
        title = if (selecting) {
            val count = state?.selected?.size ?: 0
            pluralStringResource(R.plurals.best_times_selected, count, count)
        } else {
            stringResource(R.string.best_times_title)
        },
        onBack = if (selecting) viewModel::clearSelection else onBack,
        backIcon = if (selecting) R.drawable.ic_close else R.drawable.ic_arrow_back,
        backDescription = stringResource(if (selecting) R.string.best_times_clear_selection else R.string.nav_back),
        actions = {
            if (selecting) {
                IconButton(
                    onClick = {
                        val count = viewModel.deleteSelected()
                        if (count == 0) return@IconButton
                        val message = resources.getQuantityString(R.plurals.best_times_deleted, count, count)
                        scope.launch {
                            snackbar.currentSnackbarData?.dismiss()
                            val result = snackbar.showSnackbar(message, actionLabel = undoLabel, duration = SnackbarDuration.Long)
                            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
                        }
                    },
                    modifier = Modifier.size(MinTouchTarget),
                ) {
                    Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.delete))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) {
        val ready = state ?: return@ScreenScaffold
        if (ready.rows.isEmpty()) {
            Text(stringResource(R.string.best_times_empty), style = MaterialTheme.typography.bodyLarge)
            return@ScreenScaffold
        }
        if (!selecting) {
            Text(
                stringResource(R.string.best_times_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ready.rows.forEach { ranked ->
            val id = ranked.round.id
            SelectableRoundRow(
                ranked = ranked,
                selecting = selecting,
                selected = id in ready.selected,
                onToggle = { viewModel.toggle(id) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SelectableRoundRow(ranked: RankedRound, selecting: Boolean, selected: Boolean, onToggle: () -> Unit) {
    val colors = KrantenwijkTheme.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = if (selected) BorderStroke(3.dp, colors.accentFillBorder) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = WalkTouchTarget)
            .combinedClickable(
                onClick = { if (selecting) onToggle() },
                onLongClick = onToggle,
                onLongClickLabel = stringResource(R.string.best_times_select),
            )
            .semantics(mergeDescendants = true) { this.selected = selected },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (selecting) Checkbox(checked = selected, onCheckedChange = null)
            Text(
                stringResource(R.string.best_times_rank, ranked.rank),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(min = 36.dp),
            )
            Text(
                formatRoundTime(roundSeconds(ranked.round.durationMillis)),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            Text(roundDate(ranked.round), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** "Today", or a short local date such as "Sat 26 Sep" / "za 26 sep." for when the round started. */
@Composable
private fun roundDate(round: CompletedRound): String {
    if (DateUtils.isToday(round.startedAtMillis)) return stringResource(R.string.best_times_today)
    val locale = LocalConfiguration.current.locales[0]
    return remember(round.startedAtMillis, locale) {
        SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "EEEdMMM"), locale).format(Date(round.startedAtMillis))
    }
}
