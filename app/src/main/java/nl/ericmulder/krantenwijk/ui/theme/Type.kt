package nl.ericmulder.krantenwijk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nl.ericmulder.krantenwijk.R

// Fonts are bundled (the app has no internet access); licences in assets/licenses (SIL OFL 1.1).
val BebasNeue = FontFamily(Font(R.font.bebas_neue_regular, FontWeight.Normal))

val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold),
)

private val default = Typography()

private fun TextStyle.display() = copy(fontFamily = BebasNeue, fontWeight = FontWeight.Normal, letterSpacing = 0.5.sp)

private fun TextStyle.body() = copy(fontFamily = Barlow)

/** Bebas Neue for display/headline/title-large (as in the prototype), Barlow for everything else. */
internal val KrantenwijkTypography = Typography(
    displayLarge = default.displayLarge.display(),
    displayMedium = default.displayMedium.display(),
    displaySmall = default.displaySmall.display(),
    headlineLarge = default.headlineLarge.display(),
    headlineMedium = default.headlineMedium.display(),
    headlineSmall = default.headlineSmall.display(),
    titleLarge = default.titleLarge.display().copy(fontSize = 26.sp, lineHeight = 30.sp),
    titleMedium = default.titleMedium.body().copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleSmall = default.titleSmall.body().copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = default.bodyLarge.body().copy(fontSize = 18.sp, lineHeight = 24.sp),
    bodyMedium = default.bodyMedium.body().copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodySmall = default.bodySmall.body().copy(fontSize = 14.sp),
    labelLarge = default.labelLarge.body().copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    labelMedium = default.labelMedium.body().copy(fontWeight = FontWeight.SemiBold),
    labelSmall = default.labelSmall.body().copy(fontWeight = FontWeight.SemiBold),
)
