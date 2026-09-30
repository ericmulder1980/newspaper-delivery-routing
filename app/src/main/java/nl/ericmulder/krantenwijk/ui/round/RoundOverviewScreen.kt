package nl.ericmulder.krantenwijk.ui.round

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.PrimaryActionHeight
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton
import nl.ericmulder.krantenwijk.ui.common.label
import nl.ericmulder.krantenwijk.ui.route.CountWithIcon

/** Walking route: what to take along and all sections in order (RND-A, DEC-022). */
@Composable
fun RoundOverviewScreen(
    onBack: () -> Unit,
    onOpenSection: (segmentId: Long) -> Unit,
    onEditRoute: () -> Unit,
    viewModel: RoundOverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ready = state as? RoundOverviewUiState.Ready
    ScreenScaffold(title = stringResource(R.string.round_overview_title), onBack = onBack) {
        if (ready == null) return@ScreenScaffold
        ready.routeName?.let { Text(it, style = MaterialTheme.typography.headlineLarge) }
        if (ready.sections.isEmpty()) {
            Text(stringResource(R.string.round_no_sections), style = MaterialTheme.typography.bodyLarge)
            SecondaryButton(stringResource(R.string.home_edit_route), onEditRoute)
            return@ScreenScaffold
        }
        TotalsRow(ready)
        ready.sections.forEachIndexed { index, section ->
            SectionRow(index + 1, section) { onOpenSection(section.segment.id) }
        }
        AccentButton(
            text = stringResource(R.string.round_start_first),
            onClick = { onOpenSection(ready.sections.first().segment.id) },
        )
    }
}

/** The prototype's three big numbers: sections, newspapers, leaflets. */
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

@Composable
private fun SectionRow(position: Int, section: SectionTotals, onClick: () -> Unit) {
    val segment = section.segment
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = PrimaryActionHeight)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { role = Role.Button },
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$position", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(end = 16.dp))
            Column(Modifier.weight(1f)) {
                Text(segment.streetName, style = MaterialTheme.typography.titleMedium)
                val range = if (segment.direction == Direction.DESCENDING) {
                    stringResource(R.string.section_range_reverse, segment.rangeTo, segment.rangeFrom)
                } else {
                    stringResource(R.string.section_range, segment.rangeFrom, segment.rangeTo)
                }
                Text(
                    stringResource(segment.side.label()) + " · " + range,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CountWithIcon(
                        R.drawable.ic_newspaper,
                        pluralStringResource(R.plurals.newspaper_count, section.counts.newspapers, section.counts.newspapers),
                    )
                    CountWithIcon(
                        R.drawable.ic_leaflets,
                        pluralStringResource(R.plurals.leaflet_count, section.counts.leaflets, section.counts.leaflets),
                    )
                }
            }
        }
    }
}
