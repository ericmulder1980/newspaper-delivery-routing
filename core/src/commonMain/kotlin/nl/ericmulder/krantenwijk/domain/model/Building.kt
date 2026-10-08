package nl.ericmulder.krantenwijk.domain.model

/**
 * An apartment building at one house number (plan §3.7), optionally with its own addition, e.g.
 * buildings 8A and 8B side by side (DEC-031). Its apartments are [Address]es with this building's
 * id; the building occupies the position of [label] within its segment.
 *
 * @property separator shown between house number and suffix, e.g. "" for 12A or "-" for 12-1.
 * @property addition the building's own addition, e.g. "A" for 8A; null for a plain number.
 *   Apartments of such a building store [unitPrefix] + suffix as their addition ("A-1"), so 8A-1
 *   and 8B-1 stay distinct addresses.
 */
data class Building(
    val segmentId: Long,
    val houseNumber: Int,
    val suffixType: SuffixType,
    val separator: String,
    val name: String? = null,
    val id: Long = 0,
    val addition: String? = null,
) {
    init {
        require(addition == null || addition.isNotBlank()) { "Addition must be null or non-blank" }
    }
}

/** How the building is shown and where it sorts: "12" or "8A". */
val Building.label: String get() = "$houseNumber${addition.orEmpty()}"

/** What precedes the suffix in an apartment's stored addition: "" for 12A or 12-1, "A-" for 8A-1. */
val Building.unitPrefix: String get() = addition?.let { it + separator }.orEmpty()

/** Display label of one apartment, e.g. "12A", "12-1" or "8A-1". */
fun Building.unitLabel(suffix: String): String = "$label$separator$suffix"

/** The stored addition of the apartment with [suffix]: "A" for 12A, "A-1" for 8A-1. */
fun Building.apartmentAddition(suffix: String): String = unitPrefix + suffix

/** The unit suffix of one of this building's apartments: "1" for 8A-1. */
fun Building.suffixOf(apartment: Address): String = apartment.addition.orEmpty().removePrefix(unitPrefix)
