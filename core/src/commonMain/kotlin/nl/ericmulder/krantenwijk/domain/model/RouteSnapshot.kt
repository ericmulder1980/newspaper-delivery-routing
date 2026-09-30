package nl.ericmulder.krantenwijk.domain.model

/**
 * Everything that makes up the route, without database ids: what a backup contains (DATA-02).
 * Sections are in walking order; addresses and buildings belong to their section.
 */
data class RouteSnapshot(
    val route: Route,
    val sections: List<SectionSnapshot>,
) {
    val addressCount: Int get() = sections.sumOf { s -> s.addresses.size + s.buildings.sumOf { it.apartments.size } }
}

data class SectionSnapshot(
    val segment: Segment,
    /** Standalone addresses. */
    val addresses: List<Address>,
    val buildings: List<BuildingSnapshot>,
)

data class BuildingSnapshot(val building: Building, val apartments: List<Address>)
