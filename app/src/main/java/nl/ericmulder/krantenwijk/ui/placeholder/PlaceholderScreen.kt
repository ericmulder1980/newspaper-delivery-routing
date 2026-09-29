package nl.ericmulder.krantenwijk.ui.placeholder

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton

/** A link shown on a placeholder screen so the navigation can be tried before real screens exist. */
data class PlaceholderLink(val label: String, val onClick: () -> Unit)

/** Stand-in for a screen that a later feature builds ([featureId], e.g. "ADR-C"). */
@Composable
fun PlaceholderScreen(
    title: String,
    featureId: String,
    onBack: () -> Unit,
    links: List<PlaceholderLink> = emptyList(),
) {
    ScreenScaffold(title = title, onBack = onBack) {
        Text(
            text = stringResource(R.string.placeholder_built_in, featureId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        links.forEach { SecondaryButton(text = it.label, onClick = it.onClick) }
    }
}
