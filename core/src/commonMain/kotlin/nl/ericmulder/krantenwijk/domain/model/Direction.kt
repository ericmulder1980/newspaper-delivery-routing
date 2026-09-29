package nl.ericmulder.krantenwijk.domain.model

/**
 * Walking direction through a street section, by house number. Set per segment with the
 * "walk in reverse order" option (DESCENDING); ranges themselves are always entered low to high (DEC-012).
 */
enum class Direction {
    ASCENDING,
    DESCENDING,
}
