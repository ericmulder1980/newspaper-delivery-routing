package nl.ericmulder.krantenwijk.ui.round

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.domain.rules.DeliveryCounts
import nl.ericmulder.krantenwijk.ui.route.SegmentCell
import nl.ericmulder.krantenwijk.ui.route.StickerOption
import nl.ericmulder.krantenwijk.ui.testing.FakeRoundRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeSettingsRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RoundViewModelsTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private lateinit var repo: FakeRouteRepository
    private lateinit var settings: FakeSettingsRepository
    private lateinit var rounds: FakeRoundRepository
    private var kerkEven = 0L
    private var kerkOdd = 0L
    private var molenweg = 0L

    @BeforeEach
    fun setUp() = runTest {
        repo = FakeRouteRepository()
        settings = FakeSettingsRepository()
        rounds = FakeRoundRepository()
        repo.saveRoute("Wijk 07", null)
        kerkEven = repo.addSegment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING)
        kerkOdd = repo.addSegment("Kerkstraat", Side.ODD, 1, 23, Direction.DESCENDING)
        molenweg = repo.addSegment("Molenweg", Side.ALL, 1, 10, Direction.ASCENDING)
        val kerk = repo.addresses.value.filter { it.segmentId == kerkEven }
        repo.setSticker(listOf(kerk.first { it.houseNumber == 4 }.id), Sticker.NEE_JA)
        repo.setSticker(listOf(kerk.first { it.houseNumber == 8 }.id), Sticker.NEE_NEE)
        repo.setExists(listOf(kerk.first { it.houseNumber == 20 }.id), false)
    }

    @Nested
    inner class Overview {
        @Test
        fun `totals and sections for the whole route (RND-02)`() = runTest {
            val vm = RoundOverviewViewModel(repo, rounds)
            backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
            val ready = vm.uiState.value as RoundOverviewUiState.Ready
            assertEquals("Wijk 07", ready.routeName)
            assertEquals(listOf(kerkEven, kerkOdd, molenweg), ready.sections.map { it.segment.id })
            // Kerkstraat even: 12 − 1 does not exist − 1 NEE/NEE = 10 newspapers; 1 NEE/JA → 9 leaflets.
            assertEquals(DeliveryCounts(10, 9, 10), ready.sections.first().counts)
            assertEquals(DeliveryCounts(10 + 12 + 10, 9 + 12 + 10, 32), ready.totals)
            assertNull(ready.roundInProgress)
        }

        @Test
        fun `opening a section starts the round, later sections continue it (RND-13)`() = runTest {
            val vm = RoundOverviewViewModel(repo, rounds)
            backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
            vm.startRound(kerkEven)
            rounds.now = 9_000
            vm.startRound(molenweg)
            val inProgress = (vm.uiState.value as RoundOverviewUiState.Ready).roundInProgress
            assertEquals(RoundInProgress(startedAtMillis = 1_000, resumeSegmentId = molenweg, position = 3, streetName = "Molenweg"), inProgress)
        }

        @Test
        fun `resume falls back to the first section if the last one was deleted`() = runTest {
            val vm = RoundOverviewViewModel(repo, rounds)
            backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
            vm.startRound(molenweg)
            repo.deleteSegment(molenweg)
            val inProgress = (vm.uiState.value as RoundOverviewUiState.Ready).roundInProgress
            assertEquals(kerkEven, inProgress?.resumeSegmentId)
            assertEquals(1, inProgress?.position)
        }

        @Test
        fun `abandon clears the round`() = runTest {
            val vm = RoundOverviewViewModel(repo, rounds)
            backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
            vm.startRound(kerkEven)
            vm.abandonRound()
            assertNull((vm.uiState.value as RoundOverviewUiState.Ready).roundInProgress)
            assertTrue(rounds.rounds.value.isEmpty())
        }
    }

    @Nested
    inner class Street {
        private lateinit var vm: RoundStreetViewModel

        private fun TestScope.open(segmentId: Long) {
            vm = RoundStreetViewModel(segmentId, repo, settings, rounds)
            backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
        }

        private fun ready() = vm.uiState.value as RoundStreetUiState.Ready

        private fun numbers() = ready().cells.mapNotNull { (it as? SegmentCell.House)?.address?.houseNumber }

        @Test
        fun `position, neighbours and walking order`() = runTest {
            open(kerkOdd)
            assertEquals(2, ready().position)
            assertEquals(3, ready().sectionCount)
            assertEquals(kerkEven, ready().previous?.id)
            assertEquals(molenweg, ready().next?.id)
            assertEquals((1..23 step 2).toList().reversed(), numbers())
        }

        @Test
        fun `first and last section have no previous or next`() = runTest {
            open(kerkEven)
            assertNull(ready().previous)
            open(molenweg)
            assertNull(ready().next)
        }

        @Test
        fun `skipped houses shown by default, does not exist hidden (RND-04, ADR-04)`() = runTest {
            open(kerkEven)
            assertTrue(8 in numbers())
            assertTrue(20 !in numbers())
            assertEquals(11, numbers().size)
            assertEquals(0, ready().hiddenSkipped)
        }

        @Test
        fun `hiding skipped houses keeps the counts`() = runTest {
            open(kerkEven)
            vm.setShowSkipped(false)
            assertTrue(8 !in numbers())
            assertEquals(1, ready().hiddenSkipped)
            assertEquals(DeliveryCounts(10, 9, 10), ready().counts)
            assertEquals(false, settings.settings.value.showSkipped)
        }

        @Test
        fun `does not exist is shown when the setting is on`() = runTest {
            settings.setShowNonExisting(true)
            open(kerkEven)
            assertTrue(20 in numbers())
        }

        @Test
        fun `quick sticker correction updates the counts`() = runTest {
            open(kerkEven)
            val two = ready().cells.first() as SegmentCell.House
            vm.apply(StickerOption.Set(Sticker.NEE_NEE), listOf(two.address.id))
            assertEquals(DeliveryCounts(9, 8, 9), ready().counts)
        }

        @Test
        fun `buildings stay visible as one cell`() = runTest {
            repo.createBuilding(kerkEven, 12, SuffixType.LETTER, "A", "C")
            open(kerkEven)
            assertEquals(1, ready().cells.count { it is SegmentCell.Apartments })
        }

        @Test
        fun `opening a section remembers it for Resume (DEC-030)`() = runTest {
            rounds.startRound(kerkEven)
            open(molenweg)
            backgroundScope.launch(main.dispatcher) { vm.roundActive.collect {} }
            assertEquals(molenweg, rounds.activeRound.value?.currentSegmentId)
            assertTrue(vm.roundActive.value)
        }

        @Test
        fun `finish saves the route totals once, even on a double tap`() = runTest {
            rounds.startRound(kerkEven)
            open(molenweg)
            val ids = mutableListOf<Long?>()
            vm.finishRound { ids += it }
            vm.finishRound { ids += it }
            val saved = rounds.rounds.value.single()
            assertEquals(listOf<Long?>(saved.id), ids)
            assertEquals(32, saved.newspapers)
            assertEquals(31, saved.leaflets)
        }

        @Test
        fun `finish without an active round saves nothing`() = runTest {
            open(molenweg)
            var result: Long? = -1
            vm.finishRound { result = it }
            assertNull(result)
            assertTrue(rounds.rounds.value.isEmpty())
        }

        @Test
        fun `keep screen on follows the setting (RND-08)`() = runTest {
            open(kerkEven)
            assertTrue(ready().keepScreenOn)
            settings.setKeepScreenOn(false)
            assertEquals(false, ready().keepScreenOn)
        }
    }
}
