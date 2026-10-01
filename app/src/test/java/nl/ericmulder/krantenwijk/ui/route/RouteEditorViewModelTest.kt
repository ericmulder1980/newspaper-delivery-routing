package nl.ericmulder.krantenwijk.ui.route

import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RouteEditorViewModelTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    @Test
    fun `without a route the editor asks for one, then shows it`() = runTest {
        val repo = FakeRouteRepository()
        val vm = RouteEditorViewModel(repo)
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }

        assertEquals(RouteEditorUiState.NoRoute, vm.uiState.value)
        vm.createRoute("Wijk 07", " Houten ")

        val ready = vm.uiState.value
        assertTrue(ready is RouteEditorUiState.Ready)
        assertEquals("Houten", (ready as RouteEditorUiState.Ready).route.town)
    }

    @Test
    fun `blank route name is ignored`() = runTest {
        val repo = FakeRouteRepository()
        val vm = RouteEditorViewModel(repo)
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
        vm.createRoute("  ", "")
        assertEquals(RouteEditorUiState.NoRoute, vm.uiState.value)
    }

    @Test
    fun `reorder persists the new walking order (ADR-05)`() = runTest {
        val repo = FakeRouteRepository()
        repo.saveRoute("Wijk 07", null)
        val a = repo.addSegment("Kerkstraat", Side.EVEN, 2, 8, Direction.ASCENDING)
        val b = repo.addSegment("Molenweg", Side.ALL, 1, 4, Direction.ASCENDING)
        val c = repo.addSegment("Lindelaan", Side.EVEN, 2, 6, Direction.ASCENDING)
        val vm = RouteEditorViewModel(repo)
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }

        vm.reorder(listOf(c, a, b))

        val ready = vm.uiState.value as RouteEditorUiState.Ready
        assertEquals(listOf(c, a, b), ready.segments.map { it.id })
        assertEquals(listOf(0, 1, 2), ready.segments.map { it.position })
    }
}
