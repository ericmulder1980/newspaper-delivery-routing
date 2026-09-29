package nl.ericmulder.krantenwijk.domain.model

/**
 * Part of a street walked in one go (straatdeel, plan §3.5).
 *
 * @property rangeFrom lowest number of the range it was created from (DEC-012: always <= [rangeTo]).
 * @property direction walking direction; DESCENDING is the "walk in reverse order" option.
 * @property position place in the route's walking order, starting at 0.
 */
data class Segment(
    val streetName: String,
    val side: Side,
    val rangeFrom: Int,
    val rangeTo: Int,
    val direction: Direction,
    val position: Int,
    val id: Long = 0,
)
