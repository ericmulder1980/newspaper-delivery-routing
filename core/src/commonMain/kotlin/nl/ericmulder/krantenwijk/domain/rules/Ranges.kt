package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Side

/** Largest street section that can be generated at once; longer streets are split (approved prototype). */
const val MAX_RANGE_SIZE = 300

/**
 * House numbers from [from] to [to] (inclusive) on [side], ascending.
 *
 * Ranges are entered low to high (DEC-012); walking order is applied separately via the segment's
 * direction (see [inWalkingOrder]). Endpoints that don't match the side are skipped, so 1 → 24 even
 * starts at 2.
 */
fun generateRange(from: Int, to: Int, side: Side): List<Int> {
    require(from > 0 && to > 0) { "House numbers must be positive, was $from → $to" }
    require(from <= to) { "Range must be entered low to high, was $from → $to" }
    val count = countInRange(from, to, side)
    require(count <= MAX_RANGE_SIZE) { "Range $from → $to ($side) has $count numbers, max is $MAX_RANGE_SIZE" }
    return (from..to).filter(side::matches)
}

/** Problems with a range as entered, for the live preview (ADR-11). */
sealed interface RangeIssue {
    /** Whether the range can't be saved while this issue is present. */
    val isBlocking: Boolean

    /** A number is missing, zero or negative. */
    data object InvalidNumber : RangeIssue {
        override val isBlocking = true
    }

    /** "From" is higher than "to"; ranges are entered low to high (DEC-012). */
    data object FromAfterTo : RangeIssue {
        override val isBlocking = true
    }

    /** No house numbers fall in the range on this side, e.g. 3 → 3 even. */
    data object NoNumbers : RangeIssue {
        override val isBlocking = true
    }

    /** More than [MAX_RANGE_SIZE] numbers: the section should be split. */
    data class TooManyNumbers(val count: Int) : RangeIssue {
        override val isBlocking = true
    }

    /** An endpoint is on the wrong side, e.g. 2 → 23 even; the odd endpoint is skipped. */
    data class EndpointNotOnSide(val side: Side) : RangeIssue {
        override val isBlocking = false
    }
}

/** All issues with a range as entered; empty when it can be generated without surprises. */
fun checkRange(from: Int?, to: Int?, side: Side): List<RangeIssue> {
    if (from == null || to == null || from <= 0 || to <= 0) return listOf(RangeIssue.InvalidNumber)
    if (from > to) return listOf(RangeIssue.FromAfterTo)
    val count = countInRange(from, to, side)
    return buildList {
        when {
            count == 0 -> add(RangeIssue.NoNumbers)
            count > MAX_RANGE_SIZE -> add(RangeIssue.TooManyNumbers(count))
        }
        if (!side.matches(from) || !side.matches(to)) add(RangeIssue.EndpointNotOnSide(side))
    }
}

/** Number of house numbers from [from] to [to] on [side], without generating them; 0 when from > to. */
fun countInRange(from: Int, to: Int, side: Side): Int {
    if (from > to) return 0
    val first = (from..minOf(from + 1, to)).firstOrNull(side::matches) ?: return 0
    val step = if (side == Side.ALL) 1 else 2
    return (to - first) / step + 1
}
