package nl.ericmulder.krantenwijk.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme

const val WIZARD_STEPS = 4

/** "Step 2 / 4" with the prototype's four-part bar (ADR-01). */
@Composable
fun WizardProgress(step: Int) {
    val label = stringResource(R.string.wizard_step, step, WIZARD_STEPS)
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clearAndSetSemantics { contentDescription = label },
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(WIZARD_STEPS) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(
                            if (index < step) KrantenwijkTheme.colors.accentFillBorder else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(3.dp),
                        ),
                )
            }
        }
    }
}
