package nl.ericmulder.krantenwijk.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * How one delivery state is drawn. [fill] is null for outline-only tiles: houses that receive
 * nothing stay readable (text >= 7:1, NFR-03) and are "dimmed" by style, not by contrast (DEC-015).
 */
@Immutable
data class StateStyle(
    val fill: Color?,
    val content: Color,
    val subContent: Color,
    val border: Color,
    val dashedBorder: Boolean = false,
)

/** App-specific colour roles on top of Material's colour scheme. */
@Immutable
data class KrantenwijkColors(
    val page: Color,
    /** Filled accent surfaces (e.g. the Start round button): the prototype's yellow in both themes. */
    val accentFill: Color,
    val onAccentFill: Color,
    val accentFillBorder: Color,
    val both: StateStyle,
    val newspaperOnly: StateStyle,
    val nothing: StateStyle,
    val doesNotExist: StateStyle,
)

internal val DarkKrantenwijkColors = KrantenwijkColors(
    page = Palette.Page,
    accentFill = Palette.Accent,
    onAccentFill = Palette.Ink,
    accentFillBorder = Palette.Accent,
    both = StateStyle(fill = Palette.Accent, content = Palette.Ink, subContent = Palette.Olive, border = Palette.Accent),
    newspaperOnly = StateStyle(
        fill = Palette.DarkNewspaperFill,
        content = Palette.DarkNewspaperText,
        subContent = Palette.DarkNewspaperSubtext,
        border = Palette.DarkNewspaperLine,
    ),
    nothing = StateStyle(
        fill = null,
        content = Palette.DarkNothingText,
        subContent = Palette.DarkNothingText,
        border = Palette.DarkDimLine,
    ),
    doesNotExist = StateStyle(
        fill = null,
        content = Palette.DarkGoneText,
        subContent = Palette.DarkGoneText,
        border = Palette.DarkDimLine,
        dashedBorder = true,
    ),
)

internal val LightKrantenwijkColors = KrantenwijkColors(
    page = Palette.Paper,
    accentFill = Palette.Accent,
    onAccentFill = Palette.Ink,
    // Yellow on off-white is invisible without an edge.
    accentFillBorder = Palette.Olive,
    both = StateStyle(fill = Palette.Accent, content = Palette.Ink, subContent = Palette.Olive, border = Palette.Olive),
    newspaperOnly = StateStyle(
        fill = Palette.LightNewspaperFill,
        content = Palette.LightNewspaperText,
        subContent = Palette.LightNewspaperSubtext,
        border = Palette.LightNewspaperLine,
    ),
    nothing = StateStyle(
        fill = null,
        content = Palette.LightNothingText,
        subContent = Palette.LightNothingText,
        border = Palette.LightDimLine,
    ),
    doesNotExist = StateStyle(
        fill = null,
        content = Palette.LightGoneText,
        subContent = Palette.LightGoneText,
        border = Palette.LightDimLine,
        dashedBorder = true,
    ),
)

val LocalKrantenwijkColors = staticCompositionLocalOf { DarkKrantenwijkColors }
