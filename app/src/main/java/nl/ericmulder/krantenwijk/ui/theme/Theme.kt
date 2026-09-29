package nl.ericmulder.krantenwijk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import nl.ericmulder.krantenwijk.domain.model.ThemeMode

internal val DarkScheme: ColorScheme = darkColorScheme(
    primary = Palette.Accent,
    onPrimary = Palette.Ink,
    primaryContainer = Palette.Accent,
    onPrimaryContainer = Palette.Ink,
    secondary = Palette.DarkNewspaperLine,
    onSecondary = Palette.Ink,
    background = Palette.Page,
    onBackground = Palette.Paper,
    surface = Palette.Page,
    onSurface = Palette.Paper,
    surfaceVariant = Palette.Card,
    onSurfaceVariant = Palette.DarkSecondaryText,
    surfaceContainerLowest = Palette.Page,
    surfaceContainerLow = Palette.Card,
    surfaceContainer = Palette.Card,
    surfaceContainerHigh = Palette.Card,
    surfaceContainerHighest = Palette.Card,
    outline = Palette.DarkDimLine,
    outlineVariant = Palette.Line,
    error = Palette.DarkError,
    onError = Palette.Ink,
)

internal val LightScheme: ColorScheme = lightColorScheme(
    // Yellow is unreadable as text on light backgrounds, so the light theme's accent is olive.
    primary = Palette.Olive,
    onPrimary = Palette.Paper,
    primaryContainer = Palette.Accent,
    onPrimaryContainer = Palette.Ink,
    secondary = Palette.LightNewspaperLine,
    onSecondary = Palette.White,
    background = Palette.Paper,
    onBackground = Palette.Ink,
    surface = Palette.Paper,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.White,
    onSurfaceVariant = Palette.LightSecondaryText,
    surfaceContainerLowest = Palette.White,
    surfaceContainerLow = Palette.White,
    surfaceContainer = Palette.White,
    surfaceContainerHigh = Palette.White,
    surfaceContainerHighest = Palette.White,
    outline = Palette.LightDimLine,
    outlineVariant = Palette.LightSoftLine,
    error = Palette.LightError,
    onError = Palette.White,
)

/** Whether [mode] resolves to the dark theme (SET-01). */
@Composable
@ReadOnlyComposable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun KrantenwijkTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalKrantenwijkColors provides if (dark) DarkKrantenwijkColors else LightKrantenwijkColors) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = KrantenwijkTypography,
            content = content,
        )
    }
}

/** Access to the app-specific colour roles, e.g. `KrantenwijkTheme.colors.both`. */
object KrantenwijkTheme {
    val colors: KrantenwijkColors
        @Composable @ReadOnlyComposable get() = LocalKrantenwijkColors.current
}
