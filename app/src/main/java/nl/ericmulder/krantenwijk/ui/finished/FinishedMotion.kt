package nl.ericmulder.krantenwijk.ui.finished

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * The Finished screen's motion (docs/specs/finished-screen.md): everything is a pure function of the
 * time T in seconds, so a single clock drives the whole animation and any moment can be tested.
 */
internal object FinishedMotion {
    /** Total length; the last frame is held. */
    const val DURATION = 3.3f

    /** The Next button starts fading in here; from then on it (and back) work. */
    const val NEXT_AT = 2.7f

    /** easeOutExpo: 1 − 2^(−10t), exactly 1 at the end. */
    val enter: (Float) -> Float = { t -> if (t >= 1f) 1f else 1f - 2f.pow(-10f * t) }

    /** easeOutQuart: 1 − (1−t)^4. */
    val pop: (Float) -> Float = { t -> 1f - (1f - t).pow(4) }

    /** easeInOutSine: −(cos(πt) − 1)/2. */
    val draw: (Float) -> Float = { t -> -(cos(PI * t).toFloat() - 1f) / 2f }

    /** Progress of [time] through [start]..[end], clamped to 0–1, eased, and mapped onto [from]..[to]. */
    fun tween(time: Float, start: Float, end: Float, from: Float, to: Float, ease: (Float) -> Float): Float {
        val p = ((time - start) / (end - start)).coerceIn(0f, 1f)
        return from + (to - from) * ease(p)
    }

    /** Flag wave amplitude: 26 at 0 s → 18 at 0.5 s → 10 at 1.5 s → 6 at 2.3 s, then constant. */
    fun waveAmplitude(time: Float): Float = when {
        time <= 0f -> 26f
        time < 0.5f -> lerp(26f, 18f, time / 0.5f)
        time < 1.5f -> lerp(18f, 10f, time - 0.5f)
        time < 2.3f -> lerp(10f, 6f, (time - 1.5f) / 0.8f)
        else -> 6f
    }

    /**
     * The flag's wave as one continuous curve: vertical offset (in spec px, before scaling) at
     * [column], measured in squares from the flag's left edge, fractions included. Same phase as the
     * spec's per-column wave (T·9 − c·0.7), but smooth between columns so the cloth stays joined.
     */
    fun waveOffset(time: Float, column: Float): Float = sin(time * 9f - column * 0.7f) * waveAmplitude(time)

    /** Fold shading at [column], −1 (darkest) to 1 (lightest): the wave's slope, weaker as the wave calms. */
    fun foldShade(time: Float, column: Float): Float = cos(time * 9f - column * 0.7f) * waveAmplitude(time) / 26f

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
}
