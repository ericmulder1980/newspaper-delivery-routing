package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.SuffixType

/**
 * Apartment suffixes of a building (BLD-01): letters "A".."L" or numbers "1".."20", in order.
 * Letters are single A–Z characters (case-insensitive, returned upper-case); numbers are positive.
 */
fun generateUnits(from: String, to: String, type: SuffixType): List<String> = when (type) {
    SuffixType.LETTER -> {
        val first = parseLetter(from)
        val last = parseLetter(to)
        require(first <= last) { "Letter range must go forward, was $from–$to" }
        (first..last).map(Char::toString)
    }
    SuffixType.NUMBER -> {
        val first = from.trim().toIntOrNull()
        val last = to.trim().toIntOrNull()
        require(first != null && last != null && first > 0 && last > 0) { "Unit numbers must be positive, was $from–$to" }
        require(first <= last) { "Number range must go forward, was $from–$to" }
        require(last - first + 1 <= MAX_RANGE_SIZE) { "Too many units: $from–$to" }
        (first..last).map(Int::toString)
    }
}

private fun parseLetter(value: String): Char {
    val trimmed = value.trim()
    require(trimmed.length == 1 && trimmed[0].uppercaseChar() in 'A'..'Z') { "Expected a single letter A–Z, was \"$value\"" }
    return trimmed[0].uppercaseChar()
}

/** Separator between house number and unit suffix: "" for letters (12A), "-" for numbers (12-1). */
fun separatorFor(type: SuffixType): String = when (type) {
    SuffixType.LETTER -> ""
    SuffixType.NUMBER -> "-"
}

/** The apartment suffixes for the given input, or null if it isn't a valid range (for the live preview). */
fun unitsOrNull(from: String, to: String, type: SuffixType): List<String>? =
    runCatching { generateUnits(from, to, type) }.getOrNull()
