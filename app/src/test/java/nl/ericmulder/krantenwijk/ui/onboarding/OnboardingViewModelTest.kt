package nl.ericmulder.krantenwijk.ui.onboarding

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeSettingsRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class OnboardingViewModelTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val repo = FakeRouteRepository()
    private val settings = FakeSettingsRepository()
    private val vm by lazy { OnboardingViewModel(repo, settings) }

    @Test
    fun `step 1 saves the trimmed name, blank is ignored`() = runTest {
        var next = 0
        vm.saveName("   ") { next++ }
        assertEquals(0, next)
        vm.saveName("  Sam ") { next++ }
        assertEquals(1, next)
        assertEquals("Sam", settings.settings.value.nickname)
    }

    @Test
    fun `step 2 creates the route, blank name is ignored`() = runTest {
        var next = 0
        vm.saveRoute(" ", "Houten") { next++ }
        assertNull(repo.route.value)
        vm.saveRoute("Wijk 07", "Houten") { next++ }
        assertEquals(1, next)
        assertEquals("Wijk 07", repo.route.value?.name)
    }

    @Test
    fun `state shows name, route and sections with counts`() = runTest {
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
        settings.setNickname("Sam")
        repo.saveRoute("Wijk 07", null)
        val id = repo.addSegment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING)
        val state = vm.uiState.value!!
        assertEquals("Sam", state.nickname)
        assertEquals(listOf(id), state.segments.map { it.id })
        assertEquals(12, state.addressCounts[id])
    }

    @Test
    fun `finish marks setup as completed`() = runTest {
        var finished = false
        vm.finish { finished = true }
        assertTrue(finished)
        assertTrue(settings.settings.value.onboardingCompleted)
    }

    @ParameterizedTest(name = "completed={0}, route={1} → wizard={2}")
    @CsvSource("false, false, true", "true, false, false", "false, true, false", "true, true, false")
    fun `wizard only on a real first launch`(completed: Boolean, hasRoute: Boolean, wizard: Boolean) {
        assertEquals(wizard, needsOnboarding(completed, hasRoute))
        assertFalse(needsOnboarding(onboardingCompleted = true, hasRoute = false))
    }
}
