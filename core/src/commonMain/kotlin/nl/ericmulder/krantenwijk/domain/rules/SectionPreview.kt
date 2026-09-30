package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side

/**
 * Live preview while adding a street section (ADR-11).
 *
 * @property numbers the house numbers that will be created, in walking order; empty while the
 *   range has a blocking issue.
 * @property issues everything [checkRange] reports for the entered range.
 */
data class SectionPreview(val numbers: List<Int>, val issues: List<RangeIssue>) {
    val canSave: Boolean get() = numbers.isNotEmpty() && issues.none { it.isBlocking }
}

/** Preview for a range entered low to high (DEC-012), walked in [direction]. */
fun previewSection(from: Int?, to: Int?, side: Side, direction: Direction): SectionPreview {
    val issues = checkRange(from, to, side)
    if (from == null || to == null || issues.any { it.isBlocking }) return SectionPreview(emptyList(), issues)
    val ascending = generateRange(from, to, side)
    val numbers = if (direction == Direction.DESCENDING) ascending.reversed() else ascending
    return SectionPreview(numbers, issues)
}

/**
 * A short sample of [numbers] for the preview line, as in the prototype: all of them when there
 * are at most [maxShown]; otherwise the first three and the last, with null marking the gap ("…").
 */
fun previewSample(numbers: List<Int>, maxShown: Int = 5): List<Int?> =
    if (numbers.size <= maxShown) numbers else numbers.take(3) + null + numbers.last()

/**
 * Street names from [used] that start with [query] (case-insensitive), for auto-completion
 * (ADR-07). An exact match is left out, since there is nothing left to complete.
 */
fun suggestStreetNames(used: List<String>, query: String, limit: Int = 5): List<String> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return used
        .filter { it.startsWith(q, ignoreCase = true) && !it.equals(q, ignoreCase = true) }
        .distinctBy { it.lowercase() }
        .take(limit)
}
