package nl.ericmulder.krantenwijk.ui.besttimes

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import nl.ericmulder.krantenwijk.ui.testing.FakeRoundRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class BestTimesViewModelsTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val repo = FakeRoundRepository()

    /** Round [id] took [minutes]; started [id] hours apart so start times differ. */
    private fun round(id: Long, minutes: Long) =
        CompletedRound(id, startedAtMillis = id * 3_600_000, finishedAtMillis = id * 3_600_000 + minutes * 60_000, newspapers = 57, leaflets = 41)

    private fun seed(vararg minutes: Long) {
        repo.rounds.value = minutes.mapIndexed { i, m -> round(i + 1L, m) }
    }

    @Test
    fun `top 5 is the five fastest, new round marked (RND-14)`() = runTest {
        seed(80, 65, 70, 90, 75, 72) // ids 1..6
        val vm = Top5ViewModel(roundId = 6, rounds = repo)
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
        val state = vm.uiState.value!!
        assertEquals(listOf(2L, 3L, 6L, 5L, 1L), state.top.map { it.round.id })
        assertEquals(listOf(1, 2, 3, 4, 5), state.top.map { it.rank })
        assertEquals(6L, state.newRoundId)
        assertNull(state.newOutsideTop)
    }

    @Test
    fun `a new round outside the top 5 is shown with its place`() = runTest {
        seed(60, 61, 62, 63, 64, 65, 99) // id 7 is slowest
        val vm = Top5ViewModel(roundId = 7, rounds = repo)
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
        val state = vm.uiState.value!!
        assertEquals(5, state.top.size)
        assertEquals(RankedRound(7, round(7, 99)), state.newOutsideTop)
    }

    @Test
    fun `long-press selects, tap adds, delete and undo (RND-14)`() = runTest {
        seed(80, 65, 70)
        val vm = BestTimesViewModel(repo)
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
        assertFalse(vm.uiState.value!!.selecting)
        assertEquals(listOf(2L, 3L, 1L), vm.uiState.value!!.rows.map { it.round.id })

        vm.toggle(1)
        vm.toggle(3)
        assertEquals(setOf(1L, 3L), vm.uiState.value!!.selected)
        vm.toggle(3)
        assertEquals(setOf(1L), vm.uiState.value!!.selected)
        vm.toggle(3)

        assertEquals(2, vm.deleteSelected())
        assertEquals(listOf(2L), vm.uiState.value!!.rows.map { it.round.id })
        assertFalse(vm.uiState.value!!.selecting)

        vm.undoDelete()
        assertEquals(listOf(2L, 3L, 1L), vm.uiState.value!!.rows.map { it.round.id })
        vm.undoDelete() // a second undo does nothing
        assertEquals(3, repo.rounds.value.size)
    }

    @Test
    fun `clear selection and nothing selected deletes nothing`() = runTest {
        seed(80, 65)
        val vm = BestTimesViewModel(repo)
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
        vm.toggle(1)
        vm.clearSelection()
        assertEquals(0, vm.deleteSelected())
        assertTrue(repo.rounds.value.size == 2)
    }
}
