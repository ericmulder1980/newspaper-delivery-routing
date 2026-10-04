package nl.ericmulder.krantenwijk.ui.finished

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The Finished screen's frames (RND-13): layout A fits on 5" and 6.1" screens in EN and NL. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class FinishedScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val state = FinishedUiState(name = "Sam", seconds = 4335, newspapers = 57, leaflets = 41)

    private fun show(time: Float, onNext: () -> Unit = {}) {
        compose.setContent { FinishedContent(state, time = time, onNext = onNext) }
        compose.waitForIdle()
    }

    private fun assertLastFrame(roundTime: String, papers: String, leaflets: String, next: String) {
        compose.onNodeWithContentDescription("$roundTime 1:12:15").assertIsDisplayed()
        compose.onNodeWithContentDescription("$papers 57").assertIsDisplayed()
        compose.onNodeWithContentDescription("$leaflets 41").assertIsDisplayed()
        compose.onNode(hasText(next.uppercase()) and hasClickAction()).assertIsDisplayed().assertIsEnabled()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `last frame in English on a 6-inch screen`() {
        var next = false
        show(time = FinishedMotion.DURATION, onNext = { next = true })
        compose.onNodeWithText("FINISH · SAM").assertIsDisplayed()
        compose.onNodeWithText("ROUND\nDONE").assertIsDisplayed()
        assertLastFrame("Round time", "Papers", "Leaflets", "Next")
        compose.onNode(hasText("NEXT") and hasClickAction()).performClick()
        assertTrue(next)
    }

    @Test
    @Config(qualifiers = "nl-w360dp-h640dp")
    fun `last frame in Dutch on a 5-inch screen`() {
        show(time = FinishedMotion.DURATION)
        compose.onNodeWithText("RONDE\nKLAAR").assertIsDisplayed()
        assertLastFrame("Rondetijd", "Kranten", "Folders", "Volgende")
    }

    @Test
    fun `next does not work while the animation plays`() {
        var next = false
        show(time = 1.0f, onNext = { next = true })
        compose.onNode(hasText("NEXT") and hasClickAction()).assertIsNotEnabled().performClick()
        assertTrue(!next)
    }
}
