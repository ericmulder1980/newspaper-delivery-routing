package nl.ericmulder.krantenwijk.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Every screen in plan §6. Keys are serializable so the back stack survives process death.
 * Most are placeholders until their feature is built (noted per key).
 */
@Serializable
sealed interface Destination : NavKey

/** First-launch setup wizard (ONB-A). */
@Serializable
data object Onboarding : Destination

@Serializable
data object Home : Destination

/** Segments in walking order (ADR-C). */
@Serializable
data object RouteEditor : Destination

/** One street section's addresses (ADR-B, STK-A). */
@Serializable
data class SegmentDetail(val segmentId: Long) : Destination

/** Apartment grid (BLD-B). */
@Serializable
data class BuildingDetail(val buildingId: Long) : Destination

/** Sticker, exists, exceptions, note for one address (ADR-B, ADR-09/10). */
@Serializable
data class AddressDetail(val addressId: Long) : Destination

/** Choose today's contents and see the counts (RND-A). */
@Serializable
data object RoundStart : Destination

/** Live walking list for today's contents (RND-B, DEC-014). */
@Serializable
data class RoundMode(val newspaper: Boolean, val leaflets: Boolean) : Destination

@Serializable
data object Settings : Destination

/**
 * Registers every [Destination] for saving the back stack (process death, configuration changes).
 * Sealed-class registration means a new destination can't be forgotten.
 */
@OptIn(ExperimentalSerializationApi::class)
val DestinationSerializersModule = SerializersModule {
    polymorphic(NavKey::class) { subclassesOfSealed<Destination>() }
}
