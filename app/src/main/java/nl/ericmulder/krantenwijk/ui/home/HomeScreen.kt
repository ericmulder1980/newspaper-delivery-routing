package nl.ericmulder.krantenwijk.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import nl.ericmulder.krantenwijk.ui.round.RoundOverviewUiState
import nl.ericmulder.krantenwijk.ui.round.RoundOverviewViewModel
import nl.ericmulder.krantenwijk.ui.round.SectionRowSummary

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
        AccentButton(
            text = stringResource(R.string.home_start_round),
            onClick = { onStartAt(ready.sections.first().segment.id) },
        )
        ready.sections.forEachIndexed { index, section ->
            SectionRowSummary(
                position = index + 1,
                segment = section.segment,
                onClick = { onStartAt(section.segment.id) },
                extra = { DeliveryCountsRow(section.counts) },
            )
        }
        SecondaryButton(stringResource(R.string.home_edit_route), onEditRoute)
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
