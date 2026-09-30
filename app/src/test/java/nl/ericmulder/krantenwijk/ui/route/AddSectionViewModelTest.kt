package nl.ericmulder.krantenwijk.ui.route

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.rules.RangeIssue
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AddSectionViewModelTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private lateinit var repo: FakeRouteRepository
    private lateinit var savedState: SavedStateHandle
    private lateinit var vm: AddSectionViewModel

    @BeforeEach
    fun setUp() {
        repo = FakeRouteRepository()
        savedState = SavedStateHandle()
        vm = AddSectionViewModel(repo, savedState)
    }

    private fun state() = vm.uiState.value

    private fun fill(street: String = "Kerkstraat", side: Side = Side.EVEN, from: String = "2", to: String = "24") {
        vm.setStreet(street)
        vm.setSide(side)
        vm.setFrom(from)
        vm.setTo(to)
    }

    @Test
    fun `live preview for 2 to 24 even`() = runTest {
        backgroundCollect()
        fill()
        val s = state()
        assertEquals((2..24 step 2).toList(), s.preview.numbers)
        assertTrue(s.canSave)
    }

    @Test
    fun `reverse walking reverses the preview`() = runTest {
        backgroundCollect()
        fill(side = Side.ODD, from = "1", to = "23")
        vm.setReverse(true)
        assertEquals(23, state().preview.numbers.first())
    }

    @Test
    fun `cannot save without a street name`() = runTest {
        backgroundCollect()
        fill(street = "  ")
        assertFalse(state().canSave)
    }

    @Test
    fun `high to low range blocks saving`() = runTest {
        backgroundCollect()
        fill(from = "24", to = "2")
        val s = state()
        assertEquals(listOf(RangeIssue.FromAfterTo), s.preview.issues)
        assertFalse(s.canSave)
    }

    @Test
    fun `number fields keep digits only, at most four`() = runTest {
        backgroundCollect()
        vm.setFrom("1a2-3")
        vm.setTo("123456")
        assertEquals("123", state().form.from)
        assertEquals("1234", state().form.to)
    }

    @Test
    fun `suggests street names already used in the route`() = runTest {
        repo.segments.value = listOf(Segment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING, 0, 1))
        backgroundCollect()
        vm.setStreet("ker")
        val s = state()
        assertEquals(listOf("Kerkstraat"), s.suggestions)
        assertEquals(2, s.sectionNumber)
    }

    @Test
    fun `save stores the segment with the chosen direction`() = runTest {
        backgroundCollect()
        fill(side = Side.ODD, from = "1", to = "23")
        vm.setReverse(true)
        vm.save()

        val segment = repo.segments.value.single()
        assertEquals(segment.id, vm.savedSegmentId.value)
        assertEquals(Segment("Kerkstraat", Side.ODD, 1, 23, Direction.DESCENDING, 0, 1), segment)
    }

    @Test
    fun `failed save shows an error and allows retry`() = runTest {
        backgroundCollect()
        fill()
        repo.failNextWrite = true
        vm.save()
        assertTrue(state().saveFailed)
        assertEquals(null, vm.savedSegmentId.value)

        vm.save()
        assertEquals(1L, vm.savedSegmentId.value)
    }

    @Test
    fun `form survives process death via saved state`() = runTest {
        fill(side = Side.ALL, from = "1", to = "10")
        vm.setReverse(true)

        val restored = AddSectionViewModel(repo, savedState)
        val form = restored.uiState.value.form
        assertEquals(AddSectionForm("Kerkstraat", Side.ALL, "1", "10", reverse = true), form)
    }

    /** Keeps uiState subscribed (it uses WhileSubscribed) for the duration of the test. */
    private fun kotlinx.coroutines.test.TestScope.backgroundCollect() {
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
    }
}
