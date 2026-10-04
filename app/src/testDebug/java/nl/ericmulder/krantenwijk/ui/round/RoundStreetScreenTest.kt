package nl.ericmulder.krantenwijk.ui.round

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.runBlocking
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.ui.testing.FakeRoundRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeSettingsRepository
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** DEC-022: Previous/Next name the adjacent street sections; the last one offers "Finish round". RND-13: leaving asks first. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class RoundStreetScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val repo = FakeRouteRepository()
    private val ids = runBlocking {
        repo.saveRoute("Wijk 07", null)
        listOf(
            repo.addSegment("Kerkstraat", Side.EVEN, 2, 8, Direction.ASCENDING),
            repo.addSegment("Molenweg", Side.ALL, 1, 4, Direction.ASCENDING),
        )
    }

    private val rounds = FakeRoundRepository()

    private fun show(
        segmentId: Long,
        onGoTo: (Long) -> Unit = {},
        onFinish: (Long?) -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        val vm = RoundStreetViewModel(segmentId, repo, FakeSettingsRepository(), rounds)
        compose.setContent {
            KrantenwijkTheme(dark = true) {
                RoundStreetScreen(segmentId, onBack = onBack, onGoTo = onGoTo, onFinish = onFinish, onOpenBuilding = {}, viewModel = vm)
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun `first section - no previous, next names the following street`() {
        var wentTo: Long? = null
        show(ids[0], onGoTo = { wentTo = it })
        compose.onNodeWithText("Street section 1 / 2").assertExists()
        compose.onNode(hasText("Start of route"), useUnmergedTree = true).assertExists()
        compose.onNode(hasText("Previous")).assertIsNotEnabled()
        compose.onNode(hasText("Next")).assertIsEnabled().performClick()
        assertEquals(ids[1], wentTo)
    }

    @Test
    fun `last section - finish round saves the round with the route totals (RND-13)`() {
        runBlocking { rounds.startRound(ids[0]) }
        rounds.now = 1_000 + 3_600_000
        var finishedWith: Long? = null
        show(ids[1], onFinish = { finishedWith = it })
        compose.onNode(hasText("End of route"), useUnmergedTree = true).assertExists()
        compose.onNode(hasText("Finish round")).performClick()
        compose.waitForIdle()
        val saved = rounds.rounds.value.single()
        assertEquals(saved.id, finishedWith)
        // Kerkstraat even 2–8 (4) + Molenweg 1–4 (4), all without sticker.
        assertEquals(8, saved.newspapers)
        assertEquals(8, saved.leaflets)
        assertEquals(3_600_000, saved.durationMillis)
        assertNull(rounds.activeRound.value)
    }

    @Test
    fun `back mid-round asks first - resume stays (RND-13)`() {
        runBlocking { rounds.startRound(ids[0]) }
        var wentBack = false
        show(ids[0], onBack = { wentBack = true })
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Abandon this round?").assertExists()
        compose.onNodeWithText("Resume").performClick()
        compose.onNodeWithText("Abandon this round?").assertDoesNotExist()
        assertFalse(wentBack)
        assertEquals(ids[0], rounds.activeRound.value?.currentSegmentId)
    }

    @Test
    fun `back mid-round asks first - abandon discards the round and leaves`() {
        runBlocking { rounds.startRound(ids[0]) }
        var wentBack = false
        show(ids[0], onBack = { wentBack = true })
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Abandon").performClick()
        compose.waitForIdle()
        assertTrue(wentBack)
        assertNull(rounds.activeRound.value)
        assertTrue(rounds.rounds.value.isEmpty())
    }

    @Test
    fun `without an active round back leaves at once`() {
        var wentBack = false
        show(ids[0], onBack = { wentBack = true })
        compose.onNodeWithContentDescription("Back").performClick()
        assertTrue(wentBack)
    }
}
