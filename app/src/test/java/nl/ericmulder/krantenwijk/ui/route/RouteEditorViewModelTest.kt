package nl.ericmulder.krantenwijk.ui.route

import kotlinx.coroutines.launch
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
}
