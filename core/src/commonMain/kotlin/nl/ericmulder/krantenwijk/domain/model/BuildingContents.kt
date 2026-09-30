package nl.ericmulder.krantenwijk.domain.model

/** A building with the street it's in and its apartments, sorted naturally (A, B … Z or 1, 2 … 20). */
data class BuildingContents(
    val building: Building,
    val streetName: String,
    val apartments: List<Address>,
)
