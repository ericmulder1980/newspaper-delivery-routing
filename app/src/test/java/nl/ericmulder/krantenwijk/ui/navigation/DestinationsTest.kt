package nl.ericmulder.krantenwijk.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/** Every destination must survive being saved and restored as a NavKey (process death). */
class DestinationsTest {

    private val json = Json { serializersModule = DestinationSerializersModule }

    @ParameterizedTest
    @MethodSource("destinations")
    fun `destination survives save and restore`(destination: Destination) {
        val serializer = PolymorphicSerializer(NavKey::class)
        val saved = json.encodeToString(serializer, destination)
        assertEquals(destination, json.decodeFromString(serializer, saved))
    }

    companion object {
        @JvmStatic
        fun destinations() = listOf(
            Onboarding,
            Home,
            RouteEditor,
            SegmentDetail(segmentId = 7),
            BuildingDetail(buildingId = 3),
            AddressDetail(addressId = 42),
            RoundStart,
            RoundMode(newspaper = true, leaflets = false),
            Settings,
        )
    }
}
