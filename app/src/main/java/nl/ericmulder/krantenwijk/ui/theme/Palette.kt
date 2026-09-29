package nl.ericmulder.krantenwijk.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw colours from the approved prototype, with four contrast fixes (DEC-015). Screens use the
 * semantic roles in [KrantenwijkColors] and MaterialTheme.colorScheme, never these directly.
 */
internal object Palette {
    val Ink = Color(0xFF0E0F11)
    val Page = Color(0xFF0C0D0E)
    val Card = Color(0xFF1E2126)
    val Line = Color(0xFF3A3D43)
    val Paper = Color(0xFFF4F1EA)
    val White = Color(0xFFFFFFFF)
    val Accent = Color(0xFFE8FF00)
    val Olive = Color(0xFF3A4000)

    // Dark theme text/lines (prototype values below 7:1 raised, DEC-015).
    val DarkSecondaryText = Color(0xFFB1B3B8)
    val DarkNothingText = Color(0xFFAEB1B6)
    val DarkGoneText = Color(0xFFAAADB3)
    val DarkDimLine = Color(0xFF6E7178)

    val DarkNewspaperFill = Color(0xFF0F2236)
    val DarkNewspaperText = Color(0xFFEAF4FF)
    val DarkNewspaperSubtext = Color(0xFF9CCBFF)
    val DarkNewspaperLine = Color(0xFF5AA9FF)

    // Light theme.
    val LightSecondaryText = Color(0xFF45484E)
    val LightNothingText = Color(0xFF3E4147)
    val LightGoneText = Color(0xFF484B51)
    val LightDimLine = Color(0xFF6A6D73)
    val LightSoftLine = Color(0xFFC9CBCF)

    val LightNewspaperFill = Color(0xFFD6E8FF)
    val LightNewspaperText = Color(0xFF0A2540)
    val LightNewspaperSubtext = Color(0xFF123A6B)
    val LightNewspaperLine = Color(0xFF1F5FB8)

    val DarkError = Color(0xFFFF8A80)
    val LightError = Color(0xFF9B1C1C)
}
