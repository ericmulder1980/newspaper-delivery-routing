package nl.ericmulder.krantenwijk.ui.besttimes

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import nl.ericmulder.krantenwijk.ui.testing.FakeRoundRepository
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** RND-14: Top 5 highlights the new round; Best times selects by long-press, deletes, undoes. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class BestTimesScreensTest {

    @get:Rule
    val compose = createComposeRule()

    private val repo = FakeRoundRepository().apply {
        // 2020 dates, so none of them is "today".
        val base = 1_600_000_000_000
        rounds.value = listOf(80L, 65L, 70L).mapIndexed { i, minutes ->
            val start = base + i * 86_400_000L
            CompletedRound(i + 1L, start, start + minutes * 60_000, 57, 41)
        }
    }

    @Test
    fun `top 5 marks the new round with a badge, not colour alone`() {
        var done = false
        val vm = Top5ViewModel(3, repo)
        compose.setContent {
            KrantenwijkTheme(dark = true) { Top5Screen(roundId = 3, onDone = { done = true }, viewModel = vm) }
        }
        compose.onNodeWithText("1:05:00").assertIsDisplayed()
        compose.onNodeWithText("NEW").assertIsDisplayed()
        compose.onNode(hasText("1:10:00") and hasText("NEW")).assertIsDisplayed() // merged row: round 3 is new
        compose.onNodeWithText("Done").performClick()
        assertTrue(done)
    }

    @Test
    fun `best times - hold to select, delete, undo`() {
        val vm = BestTimesViewModel(repo)
        compose.setContent {
            KrantenwijkTheme(dark = true) { BestTimesScreen(onBack = {}, viewModel = vm) }
        }
        compose.onNodeWithText("Hold a time to select it.").assertIsDisplayed()
        compose.onNodeWithText("1:20:00").performTouchInput { longClick() }
        compose.onNodeWithText("1 selected").assertIsDisplayed()
        compose.onNodeWithText("1:10:00").performClick()
        compose.onNodeWithText("2 selected").assertIsDisplayed()

        compose.onNodeWithContentDescription("Delete").performClick()
        compose.waitForIdle()
        assertEquals(listOf(2L), repo.rounds.value.map { it.id })
        compose.onNodeWithText("2 times deleted").assertIsDisplayed()

        compose.onNodeWithText("Undo").performClick()
        compose.waitForIdle()
        assertEquals(setOf(1L, 2L, 3L), repo.rounds.value.map { it.id }.toSet())
        compose.onNodeWithText("Best times").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "nl")
    fun `best times is empty at first, in Dutch`() {
        repo.rounds.value = emptyList()
        val vm = BestTimesViewModel(repo)
        compose.setContent {
            KrantenwijkTheme(dark = true) { BestTimesScreen(onBack = {}, viewModel = vm) }
        }
        compose.onNodeWithText("Nog geen rondes. Rond een ronde af om hier je tijden te zien.").assertIsDisplayed()
    }
}
