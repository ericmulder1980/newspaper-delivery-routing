package nl.ericmulder.krantenwijk.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Every screen in plan §6. Keys are serializable so the back stack survives process death.
 * Every screen is real now; the setup wizard (ONB-A) reuses AddSection and SegmentDetail.
 */
@Serializable
sealed interface Destination : NavKey

/** Setup wizard step 1: name (ONB-A, ADR-01). */
@Serializable
data object OnboardingName : Destination

/** Setup wizard step 2: route name and town. */
@Serializable
data object OnboardingRoute : Destination

/** Setup wizard step 4: the walking route so far, add more or finish. */
@Serializable
data object OnboardingSections : Destination

@Serializable
data object Home : Destination

/** Segments in walking order (ADR-C). */
@Serializable
data object RouteEditor : Destination

/** Add a street section with live preview (ADR-A); [wizard] = step 3 of the setup wizard. */
@Serializable
data class AddSection(val wizard: Boolean = false) : Destination

/** One street section's addresses (ADR-B, STK-A); [wizard] = number check in the setup wizard. */
@Serializable
data class SegmentDetail(val segmentId: Long, val wizard: Boolean = false) : Destination

/** Apartment grid (BLD-B). */
@Serializable
data class BuildingDetail(val buildingId: Long) : Destination

/** One street section while delivering (RND-B, DEC-022). */
@Serializable
data class RoundStreet(val segmentId: Long) : Destination

/** A building's mailboxes while delivering (RND-D, DEC-020). */
@Serializable
data class RoundBuilding(val buildingId: Long) : Destination

/** End-of-round animation and stats for a saved round (RND-13, DEC-030). */
@Serializable
data class Finished(val roundId: Long) : Destination

/** Top 5 round times after the Finished screen, highlighting [roundId] (RND-14). */
@Serializable
data class Top5(val roundId: Long) : Destination

@Serializable
data object Settings : Destination

/** Settings › Best times: all finished rounds, select and delete (RND-14). */
@Serializable
data object BestTimes : Destination

/**
 * Registers every [Destination] for saving the back stack (process death, configuration changes).
 * Sealed-class registration means a new destination can't be forgotten.
 */
@OptIn(ExperimentalSerializationApi::class)
val DestinationSerializersModule = SerializersModule {
    polymorphic(NavKey::class) { subclassesOfSealed<Destination>() }
}
