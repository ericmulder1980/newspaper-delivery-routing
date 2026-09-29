package nl.ericmulder.krantenwijk.domain.model

/**
 * A deliverable address: a standalone house or one apartment in a building.
 *
 * @property addition house number addition (huisnummertoevoeging), e.g. "A", "2", "bis"; null when none.
 * @property exists false when the number is marked "does not exist" (ADR-04); such addresses never receive anything.
 */
data class Address(
    val houseNumber: Int,
    val addition: String? = null,
    val exists: Boolean = true,
    val sticker: Sticker = Sticker.NONE,
    val exceptionNoNewspaper: Boolean = false,
    val exceptionNoLeaflets: Boolean = false,
) {
    init {
        require(houseNumber > 0) { "House number must be positive, was $houseNumber" }
        require(addition == null || addition.isNotBlank()) { "Addition must be null or non-blank" }
    }
}
