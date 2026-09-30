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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.rules.DeliveryCounts
import nl.ericmulder.krantenwijk.ui.common.PrimaryActionHeight
import nl.ericmulder.krantenwijk.ui.common.label
import nl.ericmulder.krantenwijk.ui.route.CountWithIcon

/** "Even · 2 → 24" or "Odd · reverse 23 → 1". */
@Composable
fun Segment.sideAndRange(): String {
    val range = if (direction == Direction.DESCENDING) {
        stringResource(R.string.section_range_reverse, rangeTo, rangeFrom)
    } else {
        stringResource(R.string.section_range, rangeFrom, rangeTo)
    }
    return stringResource(side.label()) + " · " + range
}

/** One street section in a list: position, street, side and range, plus an optional detail line. */
@Composable
fun SectionRowSummary(
    position: Int,
    segment: Segment,
    onClick: () -> Unit,
    detail: String? = null,
    extra: @Composable () -> Unit = {},
) {
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
                Text(segment.sideAndRange(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                detail?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                extra()
            }
        }
    }
}

/** Newspaper and leaflet counts with their icons. */
@Composable
fun DeliveryCountsRow(counts: DeliveryCounts) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CountWithIcon(R.drawable.ic_newspaper, pluralStringResource(R.plurals.newspaper_count, counts.newspapers, counts.newspapers))
        CountWithIcon(R.drawable.ic_leaflets, pluralStringResource(R.plurals.leaflet_count, counts.leaflets, counts.leaflets))
    }
}
