package nl.ericmulder.krantenwijk.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.math.pow

/**
 * NFR-03: text at least 7:1 against its background in both themes; tile and control edges at
 * least 3:1 against the page (WCAG 1.4.11). Guards every colour change (DEC-015).
 */
class ThemeContrastTest {

    private fun channel(c: Float): Double = if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    private fun luminance(color: Color): Double =
        0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)

    private fun contrast(a: Color, b: Color): Double {
        val (hi, lo) = listOf(luminance(a), luminance(b)).sortedDescending()
        return (hi + 0.05) / (lo + 0.05)
    }

    private fun theme(dark: Boolean): Pair<ColorScheme, KrantenwijkColors> =
        if (dark) DarkScheme to DarkKrantenwijkColors else LightScheme to LightKrantenwijkColors

    private fun assertContrast(name: String, fg: Color, bg: Color, minimum: Double, failures: MutableList<String>) {
        val ratio = contrast(fg, bg)
        if (ratio < minimum) failures += "$name: ${"%.2f".format(ratio)}:1 < $minimum:1"
    }

    @ParameterizedTest(name = "dark = {0}")
    @ValueSource(booleans = [true, false])
    fun `text meets 7 to 1`(dark: Boolean) {
        val (scheme, colors) = theme(dark)
        val failures = mutableListOf<String>()
        val text = 7.0
        assertContrast("onBackground/background", scheme.onBackground, scheme.background, text, failures)
        assertContrast("onSurface/surface", scheme.onSurface, scheme.surface, text, failures)
        assertContrast("onSurface/surfaceContainer", scheme.onSurface, scheme.surfaceContainer, text, failures)
        assertContrast("onSurfaceVariant/surfaceContainer", scheme.onSurfaceVariant, scheme.surfaceContainer, text, failures)
        assertContrast("onSurfaceVariant/background", scheme.onSurfaceVariant, scheme.background, text, failures)
        assertContrast("primary/background", scheme.primary, scheme.background, text, failures)
        assertContrast("onPrimary/primary", scheme.onPrimary, scheme.primary, text, failures)
        assertContrast("onPrimaryContainer/primaryContainer", scheme.onPrimaryContainer, scheme.primaryContainer, text, failures)
        assertContrast("onAccentFill/accentFill", colors.onAccentFill, colors.accentFill, text, failures)
        val states = mapOf(
            "both" to colors.both,
            "newspaperOnly" to colors.newspaperOnly,
            "nothing" to colors.nothing,
            "doesNotExist" to colors.doesNotExist,
        )
        for ((name, style) in states) {
            val backgrounds = style.fill?.let { listOf(it) } ?: listOf(colors.page, scheme.surfaceContainer)
            for (bg in backgrounds) {
                assertContrast("$name content", style.content, bg, text, failures)
                assertContrast("$name subContent", style.subContent, bg, text, failures)
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    @ParameterizedTest(name = "dark = {0}")
    @ValueSource(booleans = [true, false])
    fun `tile and control edges meet 3 to 1`(dark: Boolean) {
        val (scheme, colors) = theme(dark)
        val failures = mutableListOf<String>()
        val edge = 3.0
        assertContrast("outline/background", scheme.outline, scheme.background, edge, failures)
        assertContrast("accentFillBorder/page", colors.accentFillBorder, colors.page, edge, failures)
        for ((name, style) in listOf("both" to colors.both, "newspaperOnly" to colors.newspaperOnly, "nothing" to colors.nothing, "doesNotExist" to colors.doesNotExist)) {
            assertContrast("$name border/page", style.border, colors.page, edge, failures)
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }
}
