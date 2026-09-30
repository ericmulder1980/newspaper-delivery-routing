package nl.ericmulder.krantenwijk.ui.round

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.runBlocking
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeSettingsRepository
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** DEC-022: Previous/Next name the adjacent street sections; the last one offers "Finish round". */
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

    private fun show(segmentId: Long, onGoTo: (Long) -> Unit = {}, onFinish: () -> Unit = {}) {
        val vm = RoundStreetViewModel(segmentId, repo, FakeSettingsRepository())
        compose.setContent {
            KrantenwijkTheme(dark = true) {
                RoundStreetScreen(segmentId, onBack = {}, onGoTo = onGoTo, onFinish = onFinish, onOpenBuilding = {}, viewModel = vm)
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
    fun `last section - next becomes finish round`() {
        var finished = false
        show(ids[1], onFinish = { finished = true })
        compose.onNode(hasText("End of route"), useUnmergedTree = true).assertExists()
        compose.onNode(hasText("Finish round")).performClick()
        assertTrue(finished)
    }
}
