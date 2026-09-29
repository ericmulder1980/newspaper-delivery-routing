package nl.ericmulder.krantenwijk.domain.model

/** A segment with its buildings and all its addresses (standalone and apartments), in walking order. */
data class SegmentContents(
    val segment: Segment,
    val buildings: List<Building>,
    val addresses: List<Address>,
)
