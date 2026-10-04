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
            OnboardingName,
            OnboardingRoute,
            OnboardingSections,
            Home,
            RouteEditor,
            AddSection(wizard = true),
            SegmentDetail(segmentId = 7, wizard = true),
            BuildingDetail(buildingId = 3),
            RoundStreet(segmentId = 5),
            RoundBuilding(buildingId = 9),
            Finished(roundId = 4),
            Top5(roundId = 4),
            Settings,
            BestTimes,
        )
    }
}
