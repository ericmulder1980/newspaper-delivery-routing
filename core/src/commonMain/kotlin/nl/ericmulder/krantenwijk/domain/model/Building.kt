package nl.ericmulder.krantenwijk.domain.model

/**
 * An apartment building at one house number (plan §3.7). Its apartments are [Address]es with
 * this building's id; the building occupies the position of [houseNumber] within its segment.
 *
 * @property separator shown between house number and suffix, e.g. "" for 12A or "-" for 12-1.
 */
data class Building(
    val segmentId: Long,
    val houseNumber: Int,
    val suffixType: SuffixType,
    val separator: String,
    val name: String? = null,
    val id: Long = 0,
)
