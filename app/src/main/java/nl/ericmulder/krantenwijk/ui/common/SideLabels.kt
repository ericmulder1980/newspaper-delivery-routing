package nl.ericmulder.krantenwijk.ui.common

import androidx.annotation.StringRes
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.model.Side

/** Short label for a side, as on the side toggle ("Even", "Odd", "Both"). */
@StringRes
fun Side.label(): Int = when (this) {
    Side.EVEN -> R.string.side_even
    Side.ODD -> R.string.side_odd
    Side.ALL -> R.string.side_all
}
