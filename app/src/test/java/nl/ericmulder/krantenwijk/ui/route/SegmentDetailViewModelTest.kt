package nl.ericmulder.krantenwijk.ui.route

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SegmentDetailViewModelTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private lateinit var repo: FakeRouteRepository
    private lateinit var vm: SegmentDetailViewModel
    private var segmentId = 0L

    @BeforeEach
    fun setUp() = runTest {
        repo = FakeRouteRepository()
        repo.saveRoute("Wijk 07", null)
        repo.addSegment("Molenweg", Side.ALL, 1, 3, Direction.ASCENDING)
        segmentId = repo.addSegment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING)
        vm = SegmentDetailViewModel(segmentId, repo)
    }

    private fun kotlinx.coroutines.test.TestScope.collect() {
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
    }

    private fun ready() = vm.uiState.value as SegmentDetailUiState.Ready

    private fun labels() = ready().addresses.map { "${it.houseNumber}${it.addition.orEmpty()}" }

    @Test
    fun `shows the section with its position and count`() = runTest {
        collect()
        assertEquals(2, ready().position)
        assertEquals(12, ready().existingCount)
        assertEquals((2..24 step 2).map(Int::toString), labels())
    }

    @Test
    fun `does not exist is struck from the count and can be undone`() = runTest {
        collect()
        val twenty = ready().addresses.single { it.houseNumber == 20 }
        vm.setExists(twenty, false)
        assertEquals(11, ready().existingCount)
        assertEquals(12, ready().addresses.size)
        vm.setExists(twenty.copy(exists = false), true)
        assertEquals(12, ready().existingCount)
    }

    @Test
    fun `delete removes the number`() = runTest {
        collect()
        vm.delete(ready().addresses.single { it.houseNumber == 10 })
        assertEquals(false, "10" in labels())
    }

    @Test
    fun `add numbers with additions, sorted in place`() = runTest {
        collect()
        vm.addNumber("14", "b")
        vm.addNumber("14", " A ")
        vm.addNumber("26", "")
        assertEquals(listOf("12", "14", "14A", "14b", "16"), labels().subList(5, 10))
        assertEquals("26", labels().last())
        assertEquals(3, vm.added.value)
        assertNull(vm.addError.value)
    }

    @Test
    fun `duplicate and invalid numbers are reported`() = runTest {
        collect()
        vm.addNumber("12", "")
        assertEquals(AddNumberError.Duplicate("12"), vm.addError.value)
        vm.addNumber("0", "")
        assertEquals(AddNumberError.InvalidNumber, vm.addError.value)
        vm.clearAddError()
        assertNull(vm.addError.value)
        assertEquals(0, vm.added.value)
    }

    @Test
    fun `deleting the section ends in Gone`() = runTest {
        collect()
        vm.deleteSection()
        assertEquals(SegmentDetailUiState.Gone, vm.uiState.value)
        assertEquals(listOf("Molenweg"), repo.segments.value.map { it.streetName })
    }
}
