package nl.ericmulder.krantenwijk.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.MinTouchTarget
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton
import nl.ericmulder.krantenwijk.ui.round.DeliveryCountsRow
import nl.ericmulder.krantenwijk.ui.round.RoundInProgress
import nl.ericmulder.krantenwijk.ui.round.RoundOverviewUiState
import nl.ericmulder.krantenwijk.ui.round.RoundOverviewViewModel
import nl.ericmulder.krantenwijk.ui.round.SectionRowSummary
import nl.ericmulder.krantenwijk.ui.route.ConfirmDialog
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import java.util.Date

/**
 * Home = the prototype's main screen ("1 · Looproute"): route, what to take along, all sections in
 * walking order, Start round and Edit route (plan §6, RND-A, DEC-022).
 */
@Composable
fun HomeScreen(
    onStartAt: (segmentId: Long) -> Unit,
    onEditRoute: () -> Unit,
    onSettings: () -> Unit,
    viewModel: RoundOverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ready = state as? RoundOverviewUiState.Ready
    var askAbandon by rememberSaveable { mutableStateOf(false) }
    if (askAbandon) {
        ConfirmDialog(
            title = stringResource(R.string.round_abandon_title),
            text = stringResource(R.string.home_abandon_message),
            confirmLabel = stringResource(R.string.round_abandon),
            onConfirm = {
                askAbandon = false
                viewModel.abandonRound()
            },
            onDismiss = { askAbandon = false },
        )
    }
    ScreenScaffold(title = stringResource(R.string.app_name), onBack = null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.round_overview_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(ready?.routeName ?: stringResource(R.string.home_no_route), style = MaterialTheme.typography.headlineLarge)
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(MinTouchTarget)) {
                Icon(painterResource(R.drawable.ic_settings), contentDescription = stringResource(R.string.settings_title))
            }
        }
        if (ready == null) return@ScreenScaffold
        if (ready.sections.isEmpty()) {
            Text(stringResource(R.string.round_no_sections), style = MaterialTheme.typography.bodyLarge)
            AccentButton(stringResource(R.string.home_edit_route), onEditRoute)
            return@ScreenScaffold
        }
        TotalsRow(ready)
        // Opening a section starts the timer, or continues the round in progress (DEC-030).
        val open = { segmentId: Long ->
            viewModel.startRound(segmentId)
            onStartAt(segmentId)
        }
        val inProgress = ready.roundInProgress
        if (inProgress == null) {
            AccentButton(
                text = stringResource(R.string.home_start_round),
                onClick = { open(ready.sections.first().segment.id) },
            )
        } else {
            RoundInProgressCard(inProgress, sectionCount = ready.sections.size)
            AccentButton(stringResource(R.string.home_resume_round), onClick = { open(inProgress.resumeSegmentId) })
            SecondaryButton(stringResource(R.string.home_abandon_round), onClick = { askAbandon = true })
        }
        ready.sections.forEachIndexed { index, section ->
            SectionRowSummary(
                position = index + 1,
                segment = section.segment,
                onClick = { open(section.segment.id) },
                extra = { DeliveryCountsRow(section.counts) },
            )
        }
        SecondaryButton(stringResource(R.string.home_edit_route), onEditRoute)
    }
}

/** "Round in progress": when it started and where Resume goes, no running clock (RND-13, wireframe G). */
@Composable
private fun RoundInProgressCard(round: RoundInProgress, sectionCount: Int) {
    val context = LocalContext.current
    val startedAt = remember(round.startedAtMillis) { DateFormat.getTimeFormat(context).format(Date(round.startedAtMillis)) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(2.dp, KrantenwijkTheme.colors.accentFillBorder),
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(stringResource(R.string.home_round_in_progress), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.home_round_in_progress_detail, startedAt, round.position, sectionCount, round.streetName),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/** The prototype's three big numbers: sections, newspapers, leaflets (what to take along, RND-02). */
@Composable
private fun TotalsRow(ready: RoundOverviewUiState.Ready) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            ready.sections.size to R.string.round_total_sections,
            ready.totals.newspapers to R.string.round_total_newspapers,
            ready.totals.leaflets to R.string.round_total_leaflets,
        ).forEach { (value, label) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {},
            ) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value.toString(), style = MaterialTheme.typography.displaySmall)
                    Text(stringResource(label), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
