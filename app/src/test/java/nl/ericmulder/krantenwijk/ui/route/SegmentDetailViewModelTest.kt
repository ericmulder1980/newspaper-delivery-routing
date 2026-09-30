package nl.ericmulder.krantenwijk.ui.route

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.rules.DeliveryCounts
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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

    private fun TestScope.collect() {
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
    }

    private fun ready() = vm.uiState.value as SegmentDetailUiState.Ready

    private fun labels() = ready().addresses.map { "${it.houseNumber}${it.addition.orEmpty()}" }

    private fun id(number: Int) = ready().addresses.single { it.houseNumber == number && it.addition == null }.id

    @Test
    fun `shows the section with its position and counts`() = runTest {
        collect()
        assertEquals(2, ready().position)
        assertEquals(12, ready().existingCount)
        assertEquals(DeliveryCounts(newspapers = 12, leaflets = 12, deliverableAddresses = 12), ready().counts)
        assertEquals((2..24 step 2).map(Int::toString), labels())
    }

    @Nested
    inner class Stickers {
        @Test
        fun `sticker changes the counts (STK-01, STK-03)`() = runTest {
            collect()
            vm.apply(StickerOption.Set(Sticker.NEE_JA), listOf(id(4)))
            vm.apply(StickerOption.Set(Sticker.NEE_NEE), listOf(id(8)))
            assertEquals(DeliveryCounts(newspapers = 11, leaflets = 10, deliverableAddresses = 11), ready().counts)
            assertEquals(Sticker.NEE_JA, ready().addresses.single { it.id == id(4) }.sticker)
        }

        @Test
        fun `does not exist keeps the sticker and a sticker restores the number`() = runTest {
            collect()
            val twenty = id(20)
            vm.apply(StickerOption.Set(Sticker.NEE_JA), listOf(twenty))
            vm.apply(StickerOption.DoesNotExist, listOf(twenty))
            val gone = ready().addresses.single { it.id == twenty }
            assertFalse(gone.exists)
            assertEquals(Sticker.NEE_JA, gone.sticker)
            assertEquals(StickerOption.DoesNotExist, gone.currentOption())
            assertEquals(11, ready().existingCount)

            vm.apply(StickerOption.Set(Sticker.JA), listOf(twenty))
            val back = ready().addresses.single { it.id == twenty }
            assertTrue(back.exists)
            assertEquals(StickerOption.Set(Sticker.JA), back.currentOption())
        }
    }

    @Nested
    inner class Bulk {
        @Test
        fun `select numbers and set their sticker at once (STK-02)`() = runTest {
            collect()
            vm.startSelecting()
            listOf(2, 4, 6, 8, 10).forEach { n -> vm.toggleSelected(ready().addresses.single { it.houseNumber == n }) }
            val selected = ready().selection!!
            assertEquals(5, selected.size)

            vm.apply(StickerOption.Set(Sticker.NEE_NEE), selected)

            assertEquals(5, ready().addresses.count { it.sticker == Sticker.NEE_NEE })
            assertNull(ready().selection)
        }

        @Test
        fun `toggling twice deselects, select all takes every number`() = runTest {
            collect()
            vm.startSelecting()
            val a = ready().addresses.first()
            vm.toggleSelected(a)
            vm.toggleSelected(a)
            assertEquals(emptySet<Long>(), ready().selection)
            vm.selectAll()
            assertEquals(12, ready().selection!!.size)
            vm.stopSelecting()
            assertFalse(ready().selecting)
        }

        @Test
        fun `bulk delete removes the selection and ends selecting`() = runTest {
            collect()
            vm.startSelecting()
            vm.toggleSelected(ready().addresses[0])
            vm.toggleSelected(ready().addresses[1])
            vm.delete(ready().selection!!)
            assertEquals(10, ready().addresses.size)
            assertNull(ready().selection)
        }

        @Test
        fun `numbers deleted elsewhere drop out of the selection`() = runTest {
            collect()
            vm.startSelecting()
            val first = ready().addresses[0]
            vm.toggleSelected(first)
            repo.deleteAddresses(listOf(first.id))
            assertEquals(emptySet<Long>(), ready().selection)
        }
    }

    @Nested
    inner class Numbers {
        @Test
        fun `delete removes the number`() = runTest {
            collect()
            vm.delete(listOf(id(10)))
            assertFalse("10" in labels())
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
    }

    @Test
    fun `deleting the section ends in Gone`() = runTest {
        collect()
        vm.deleteSection()
        assertEquals(SegmentDetailUiState.Gone, vm.uiState.value)
        assertEquals(listOf("Molenweg"), repo.segments.value.map { it.streetName })
    }
}
