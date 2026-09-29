package nl.ericmulder.krantenwijk.domain.model

/**
 * A deliverable address: a standalone house or one apartment in a building.
 *
 * @property id database id; 0 for an address that hasn't been stored yet.
 * @property buildingId the apartment building this address belongs to, or null for a standalone address.
 * @property addition house number addition (huisnummertoevoeging), e.g. "A", "2", "bis"; null when none.
 *   For apartments this is the unit suffix; the building's separator is added for display only.
 * @property exists false when the number is marked "does not exist" (ADR-04); such addresses never receive anything.
 * @property note free-text note, e.g. "mailbox at side door" (ADR-10).
 */
data class Address(
    val houseNumber: Int,
    val addition: String? = null,
    val exists: Boolean = true,
    val sticker: Sticker = Sticker.NONE,
    val exceptionNoNewspaper: Boolean = false,
    val exceptionNoLeaflets: Boolean = false,
    val note: String? = null,
    val id: Long = 0,
    val segmentId: Long = 0,
    val buildingId: Long? = null,
) {
    init {
        require(houseNumber > 0) { "House number must be positive, was $houseNumber" }
        require(addition == null || addition.isNotBlank()) { "Addition must be null or non-blank" }
    }
}
