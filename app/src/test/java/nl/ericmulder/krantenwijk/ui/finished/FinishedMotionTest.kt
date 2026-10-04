package nl.ericmulder.krantenwijk.ui.finished

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.domain.model.AppSettings
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.draw
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.enter
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.pop
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.tween
import nl.ericmulder.krantenwijk.ui.testing.FakeRoundRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeSettingsRepository
import nl.ericmulder.krantenwijk.ui.testing.MainDispatcherExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/** docs/specs/finished-screen.md: the easings, tween and wave amplitude. */
class FinishedMotionTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val e = 1e-4f

    @Test
    fun `easings start at 0 and end at 1`() {
        listOf(enter, pop, draw).forEach { ease ->
            assertEquals(0f, ease(0f), e)
            assertEquals(1f, ease(1f), e)
        }
        assertEquals(0.5f, draw(0.5f), e) // easeInOutSine is symmetric
        assertEquals(1f - 0.5f * 0.5f * 0.5f * 0.5f, pop(0.5f), e)
        assertEquals(1f - 1f / 32f, enter(0.5f), e)
    }

    @Test
    fun `tween clamps before the start and after the end`() {
        assertEquals(10f, tween(0f, 1f, 2f, 10f, 20f, pop), e)
        assertEquals(20f, tween(5f, 1f, 2f, 10f, 20f, pop), e)
        assertEquals(15f, tween(1.5f, 1f, 2f, 10f, 20f, draw), e)
    }

    @Test
    fun `flag slides in from the left`() {
        assertEquals(-470f, tween(0f, 0f, 0.7f, -470f, 0f, enter), e)
        assertEquals(0f, tween(0.7f, 0f, 0.7f, -470f, 0f, enter), e)
    }

    @Test
    fun `wave amplitude follows the keyframes`() {
        assertEquals(26f, FinishedMotion.waveAmplitude(0f), e)
        assertEquals(22f, FinishedMotion.waveAmplitude(0.25f), e)
        assertEquals(18f, FinishedMotion.waveAmplitude(0.5f), e)
        assertEquals(10f, FinishedMotion.waveAmplitude(1.5f), e)
        assertEquals(6f, FinishedMotion.waveAmplitude(2.3f), e)
        assertEquals(6f, FinishedMotion.waveAmplitude(9f), e)
    }

    @Test
    fun `view model shows name, rounded seconds and counts`() = runTest {
        val rounds = FakeRoundRepository(now = 0)
        rounds.startRound(1)
        rounds.now = 4_335_400
        val id = rounds.finishRound(57, 41)!!
        val vm = FinishedViewModel(id, rounds, FakeSettingsRepository(AppSettings(nickname = "Sam")))
        backgroundScope.launch(main.dispatcher) { vm.uiState.collect {} }
        assertEquals(FinishedUiState(name = "Sam", seconds = 4335, newspapers = 57, leaflets = 41), vm.uiState.value)

        val missing = FinishedViewModel(99, rounds, FakeSettingsRepository())
        backgroundScope.launch(main.dispatcher) { missing.uiState.collect {} }
        assertNull(missing.uiState.value)
    }
}
