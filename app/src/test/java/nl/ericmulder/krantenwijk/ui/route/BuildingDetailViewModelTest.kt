package nl.ericmulder.krantenwijk.ui.route

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class BuildingDetailViewModelTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private lateinit var repo: FakeRouteRepository
    private lateinit var vm: BuildingDetailViewModel
    private var buildingId = 0L

    @BeforeEach
    fun setUp() = runTest {
        repo = FakeRouteRepository()
        repo.saveRoute("Wijk 07", null)
        val segmentId = repo.addSegment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING)
        buildingId = repo.createBuilding(segmentId, 12, SuffixType.LETTER, "A", "L")
        vm = BuildingDetailViewModel(buildingId, repo)
    }

    private fun TestScope.collect() {
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
    }

    private fun ready() = vm.uiState.value as BuildingDetailUiState.Ready

    private fun suffixes() = ready().apartments.map { it.addition }

    private fun id(suffix: String) = ready().apartments.single { it.addition == suffix }.id

    @Test
    fun `shows street, apartments A to L and counts (BLD-03)`() = runTest {
        collect()
        assertEquals("Kerkstraat", ready().streetName)
        assertEquals(('A'..'L').map(Char::toString), suffixes())
        assertEquals(12, ready().existingCount)
        assertEquals(12, ready().counts.leaflets)
    }

    @Test
    fun `sticker per apartment`() = runTest {
        collect()
        vm.apply(StickerOption.Set(Sticker.NEE_JA), listOf(id("C")))
        assertEquals(Sticker.NEE_JA, ready().apartments.single { it.addition == "C" }.sticker)
        assertEquals(11, ready().counts.leaflets)
    }

    @Test
    fun `select 12A to 12F and set NEE-NEE updates exactly those six (BLD-04)`() = runTest {
        collect()
        vm.startSelecting()
        ('A'..'F').forEach { vm.toggleSelected(ready().apartments.single { a -> a.addition == it.toString() }) }
        vm.apply(StickerOption.Set(Sticker.NEE_NEE), ready().selection!!)
        assertEquals(('A'..'F').map(Char::toString), ready().apartments.filter { it.sticker == Sticker.NEE_NEE }.map { it.addition })
        assertNull(ready().selection)
    }

    @Test
    fun `select all and mark does not exist`() = runTest {
        collect()
        vm.startSelecting()
        vm.selectAll()
        vm.apply(StickerOption.DoesNotExist, ready().selection!!)
        assertEquals(0, ready().existingCount)
        assertFalse(ready().apartments.any { it.exists })
    }

    @Test
    fun `add 12M after 12L and delete an apartment (BLD-05)`() = runTest {
        collect()
        vm.addApartment("m")
        assertEquals("M", suffixes().last())
        assertEquals(1, vm.added.value)
        vm.delete(listOf(id("B")))
        assertFalse("B" in suffixes())
        assertEquals(12, ready().apartments.size)
    }

    @Test
    fun `invalid and duplicate apartments are reported`() = runTest {
        collect()
        vm.addApartment("7")
        assertEquals(AddApartmentError.Invalid, vm.addError.value)
        vm.addApartment("c")
        assertEquals(AddApartmentError.Duplicate("12C"), vm.addError.value)
        assertEquals(0, vm.added.value)
    }

    @Test
    fun `removing the building ends in Gone`() = runTest {
        collect()
        repo.removeBuilding(buildingId)
        assertEquals(BuildingDetailUiState.Gone, vm.uiState.value)
    }
}
