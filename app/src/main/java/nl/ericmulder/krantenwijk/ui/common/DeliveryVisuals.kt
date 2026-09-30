package nl.ericmulder.krantenwijk.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.DeliveryKind
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import nl.ericmulder.krantenwijk.ui.theme.StateStyle

/**
 * Colours for a delivery state. LEAFLETS_ONLY can't occur in the editor yet (it needs exceptions,
 * ADR-09, or a leaflets-only round); it borrows the "both" style until round mode gives it its own.
 */
@Composable
@ReadOnlyComposable
fun DeliveryKind.style(): StateStyle {
    val colors = KrantenwijkTheme.colors
    return when (this) {
        DeliveryKind.BOTH, DeliveryKind.LEAFLETS_ONLY -> colors.both
        DeliveryKind.NEWSPAPER_ONLY -> colors.newspaperOnly
        DeliveryKind.NOTHING -> colors.nothing
        DeliveryKind.DOES_NOT_EXIST -> colors.doesNotExist
    }
}

@StringRes
fun DeliveryKind.label(): Int = when (this) {
    DeliveryKind.BOTH -> R.string.delivery_both
    DeliveryKind.NEWSPAPER_ONLY -> R.string.delivery_newspaper_only
    DeliveryKind.LEAFLETS_ONLY -> R.string.delivery_leaflets_only
    DeliveryKind.NOTHING -> R.string.delivery_nothing
    DeliveryKind.DOES_NOT_EXIST -> R.string.does_not_exist
}

/** Full sticker name, e.g. "NEE/JA sticker". */
@StringRes
fun Sticker.label(): Int = when (this) {
    Sticker.NONE -> R.string.sticker_none
    Sticker.JA -> R.string.sticker_ja
    Sticker.NEE_JA -> R.string.sticker_nee_ja
    Sticker.NEE_NEE -> R.string.sticker_nee_nee
}

/** Short sticker label for tiles, e.g. "NEE/JA". */
@StringRes
fun Sticker.shortLabel(): Int = when (this) {
    Sticker.NONE -> R.string.sticker_short_none
    Sticker.JA -> R.string.sticker_short_ja
    Sticker.NEE_JA -> R.string.sticker_short_nee_ja
    Sticker.NEE_NEE -> R.string.sticker_short_nee_nee
}

/**
 * The icons for a delivery state: newspaper and/or leaflets, or a "nothing" sign. Together with the
 * text labels this keeps status from depending on colour alone (NFR-04). Icons are decorative for
 * TalkBack; the surrounding element carries the spoken label.
 */
@Composable
fun DeliveryIcons(kind: DeliveryKind, tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val newspaper = kind == DeliveryKind.BOTH || kind == DeliveryKind.NEWSPAPER_ONLY
        val leaflets = kind == DeliveryKind.BOTH || kind == DeliveryKind.LEAFLETS_ONLY
        if (newspaper) Icon(painterResource(R.drawable.ic_newspaper), null, Modifier.size(size), tint)
        if (leaflets) Icon(painterResource(R.drawable.ic_leaflets), null, Modifier.size(size), tint)
        if (kind == DeliveryKind.NOTHING) Icon(painterResource(R.drawable.ic_nothing), null, Modifier.size(size), tint)
    }
}
