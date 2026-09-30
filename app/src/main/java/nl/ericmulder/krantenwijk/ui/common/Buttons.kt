package nl.ericmulder.krantenwijk.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme

private val ButtonShape = RoundedCornerShape(16.dp)

/** The prototype's big yellow action button (e.g. "Start round"). */
@Composable
fun AccentButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = KrantenwijkTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = PrimaryActionHeight),
        shape = ButtonShape,
        border = if (enabled) BorderStroke(2.dp, colors.accentFillBorder) else null,
        colors = ButtonDefaults.buttonColors(containerColor = colors.accentFill, contentColor = colors.onAccentFill),
    ) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = MinTouchTarget),
        shape = ButtonShape,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
