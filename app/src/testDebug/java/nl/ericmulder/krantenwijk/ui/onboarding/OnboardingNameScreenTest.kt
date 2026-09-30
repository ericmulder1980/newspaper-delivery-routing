package nl.ericmulder.krantenwijk.ui.onboarding

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeSettingsRepository
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** ADR-01: "Next" is disabled until the required field is filled; progress shows "Step 1 / 4". */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class OnboardingNameScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `next is enabled only after entering a name`() {
        val settings = FakeSettingsRepository()
        val vm = OnboardingViewModel(FakeRouteRepository(), settings)
        var next = 0
        compose.setContent { KrantenwijkTheme(dark = true) { OnboardingNameScreen(onNext = { next++ }, viewModel = vm) } }

        compose.onNodeWithText("Step 1 / 4", useUnmergedTree = true).assertExists()
        compose.onNode(hasText("Next")).assertIsNotEnabled()
        compose.onNode(hasSetTextAction()).performTextInput("Sam")
        compose.onNode(hasText("Next")).assertIsEnabled().performClick()
        compose.waitForIdle()

        assertEquals(1, next)
        assertEquals("Sam", settings.settings.value.nickname)
    }
}
