package nl.ericmulder.krantenwijk.ui.finished

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.domain.rules.formatRoundTime
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.DURATION
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.NEXT_AT
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.draw
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.enter
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.pop
import nl.ericmulder.krantenwijk.ui.finished.FinishedMotion.tween
import nl.ericmulder.krantenwijk.ui.theme.Barlow
import nl.ericmulder.krantenwijk.ui.theme.BebasNeue
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.tan

// Fixed colours from the spec: this celebration screen looks the same in light and dark theme.
// Yellow and off-white on the near-black ground are well above the 7:1 outdoor contrast rule.
private val Ground = Color(0xFF0C0D0E)
private val Ink = Color(0xFF0E0F11)
private val Yellow = Color(0xFFE8FF00)
private val OffWhite = Color(0xFFF4F1EA)
private val Card = Color(0xFF1E2126)
private val Hairline = Color(0xFF2E3137)
private val Grey = Color(0xFFA7A9AE)
private val Track = Color(0xFF2A2D33)

/** The spec is drawn on a 390 × 844 canvas; its px are treated as dp and scaled to the screen width. */
private const val SPEC_WIDTH = 390f

/**
 * End of a round (RND-13, DEC-030): a 3.3 s animation built from docs/specs/finished-screen.md, then
 * the round time and counts. No sound and not skippable; back does nothing until Next appears.
 * With the system's "Remove animations" on, Compose finishes the clock at once: the last frame shows.
 */
@Composable
fun FinishedScreen(
    roundId: Long,
    onNext: () -> Unit,
    viewModel: FinishedViewModel = hiltViewModel<FinishedViewModel, FinishedViewModel.Factory>(
        key = "finished-$roundId",
        creationCallback = { it.create(roundId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ready = state
    if (ready == null) {
        Box(Modifier.fillMaxSize().background(Ground))
        return
    }
    // Played once: after rotation or process death the last frame is held instead of replaying.
    var played by rememberSaveable { mutableStateOf(false) }
    val clock = remember { Animatable(if (played) DURATION else 0f) }
    LaunchedEffect(Unit) {
        clock.animateTo(DURATION, tween((DURATION * 1000).roundToInt(), easing = LinearEasing))
        played = true
    }
    FinishedContent(ready, time = clock.value, onNext = onNext)
}

/** The screen at moment [time] (seconds); split out so tests can render any frame. */
@Composable
internal fun FinishedContent(state: FinishedUiState, time: Float, onNext: () -> Unit) {
    val nextEnabled = time >= NEXT_AT
    BackHandler { if (nextEnabled) onNext() }

    BoxWithConstraints(Modifier.fillMaxSize().background(Ground)) {
        val scale = maxWidth.value / SPEC_WIDTH
        // Short screens (about 5"): smaller flag band, headline and tiles so everything fits.
        val compact = maxHeight < 760.dp
        val band = if (compact) maxHeight * 0.14f else (170 * scale).dp
        val headlineSize = ((if (compact) 64 else 96) * scale).dpAsSp()
        val tileHeight = if (compact) 88.dp else 104.dp

        Canvas(Modifier.fillMaxSize()) {
            drawFlag(time, scale = size.width / SPEC_WIDTH, band = band.toPx())
            drawSpeedSlashes(time, scale = size.width / SPEC_WIDTH)
        }

        Column(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = (20 * scale).dp)) {
            Spacer(Modifier.height(band + (44 * scale).dp))
            Eyebrow(state.name, time)
            Spacer(Modifier.height(12.dp))
            Headline(time, headlineSize)
            Spacer(Modifier.weight(1f).heightIn(min = 16.dp))
            Stats(state, time, tileHeight, scale)
            Spacer(Modifier.height(16.dp))
            NextButton(time, enabled = nextEnabled, onClick = onNext, scale = scale)
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Checkered flag: slides in, waves, then lifts into a band at the top. The spec moves each column
 * separately (translate + skew); that reads as loose diagonal strips, so the flag is drawn as one
 * surface instead: every point follows the same continuous wave, with light fold shading.
 */
private fun DrawScope.drawFlag(time: Float, scale: Float, band: Float) {
    val square = 39f * scale
    val rows = ceil(size.height / square).toInt() + 2
    val top = -square + tween(time, 0.9f, 1.5f, 0f, -(rows * square - square - band), draw)
    val left = tween(time, 0f, 0.7f, -470f, 0f, enter) * scale
    val bottom = top + rows * square
    val fadeHeight = 78f * scale

    // Sample the wave across the 10 columns; finer than a square so the curve looks smooth.
    val samples = FLAG_COLUMNS * SLICES_PER_SQUARE
    val xs = FloatArray(samples + 1) { left + it * square / SLICES_PER_SQUARE }
    val dys = FloatArray(samples + 1) { FinishedMotion.waveOffset(time, it.toFloat() / SLICES_PER_SQUARE) * scale }

    fun Path.band(from: Int, to: Int, y0: Float, y1: Float) {
        moveTo(xs[from], y0 + dys[from])
        for (k in from + 1..to) lineTo(xs[k], y0 + dys[k])
        for (k in to downTo from) lineTo(xs[k], y1 + dys[k])
        close()
    }

    val body = Path().apply { band(0, samples, top, bottom) }
    drawPath(body, OffWhite)
    val ink = Path()
    for (column in 0 until FLAG_COLUMNS) {
        for (row in 0 until rows) {
            if ((row + column) % 2 != 0) continue
            val y = top + row * square
            ink.band(column * SLICES_PER_SQUARE, (column + 1) * SLICES_PER_SQUARE, y, y + square)
        }
    }
    drawPath(ink, Ink)

    // Folds: slopes facing up catch light, slopes facing down fall into shadow.
    val shading = Array(samples + 1) { k ->
        val shade = FinishedMotion.foldShade(time, k.toFloat() / SLICES_PER_SQUARE)
        k.toFloat() / samples to if (shade >= 0) Color.White.copy(alpha = 0.10f * shade) else Color.Black.copy(alpha = -0.28f * shade)
    }
    drawPath(body, Brush.horizontalGradient(*shading, startX = xs.first(), endX = xs.last()))

    val fade = Path().apply { band(0, samples, bottom, bottom + fadeHeight) }
    drawPath(fade, Brush.verticalGradient(listOf(Ink, Ground), startY = bottom, endY = bottom + fadeHeight))
}

private const val FLAG_COLUMNS = 10
private const val SLICES_PER_SQUARE = 6

/** Three yellow speed slashes that streak across and fade (spec §2). */
private fun DrawScope.drawSpeedSlashes(time: Float, scale: Float) {
    val heightScale = size.height / 844f
    val slashHeight = 11f * scale
    listOf(
        Triple(40f, 300f, 180f) to 0.05f,
        Triple(120f, 330f, 120f) to 0.14f,
        Triple(0f, 560f, 220f) to 0.22f,
    ).forEach { (geometry, delay) ->
        val (x, y, width) = geometry
        val progress = tween(time, delay, delay + 0.5f, 0f, 1f, enter)
        val alpha = tween(time, delay + 0.45f, delay + 0.75f, 1f, 0f, draw)
        if (alpha <= 0f) return@forEach
        val left = (x + (1 - progress) * -420f + progress * 60f) * scale
        drawSlash(Offset(left, y * heightScale), width * scale, slashHeight, Yellow, alpha)
    }
}

/** A parallelogram leaning right like CSS skewX(−24°): the prototype's yellow slash. */
private fun DrawScope.drawSlash(topLeft: Offset, width: Float, height: Float, color: Color, alpha: Float) {
    val lean = tan(Math.toRadians(24.0)).toFloat() * height
    val path = Path().apply {
        moveTo(topLeft.x + lean, topLeft.y)
        lineTo(topLeft.x + width, topLeft.y)
        lineTo(topLeft.x + width - lean, topLeft.y + height)
        lineTo(topLeft.x, topLeft.y + height)
        close()
    }
    drawPath(path, color, alpha = alpha)
}

@Composable
private fun Eyebrow(name: String?, time: Float) {
    val e = tween(time, 1.30f, 1.70f, 0f, 1f, enter)
    val text = if (name != null) stringResource(R.string.finished_eyebrow, name) else stringResource(R.string.finished_eyebrow_no_name)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.graphicsLayer {
            alpha = e
            translationX = (1 - e) * -24.dp.toPx()
        },
    ) {
        Canvas(Modifier.size(18.dp, 11.dp)) { drawSlash(Offset.Zero, size.width, size.height, Yellow, alpha = 1f) }
        Text(
            text.uppercase(),
            style = TextStyle(fontFamily = BebasNeue, fontSize = 19.sp, letterSpacing = 0.14.em, color = Yellow),
        )
    }
}

@Composable
private fun Headline(time: Float, size: TextUnit) {
    val alpha = tween(time, 1.15f, 1.50f, 0f, 1f, pop)
    val scale = tween(time, 1.15f, 1.60f, 1.18f, 1f, pop)
    Text(
        stringResource(R.string.finished_headline).uppercase(),
        style = TextStyle(fontFamily = BebasNeue, fontStyle = FontStyle.Italic, fontSize = size, lineHeight = size * 0.92f, color = OffWhite),
        modifier = Modifier
            .semantics { heading() }
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0.5f)
            },
    )
}

@Composable
private fun Stats(state: FinishedUiState, time: Float, tileHeight: Dp, scale: Float) {
    val seconds = tween(time, 2.0f, 2.8f, state.seconds * 0.6f, state.seconds.toFloat(), pop).roundToLong()
    val papers = tween(time, 2.15f, 2.9f, 0f, state.newspapers.toFloat(), pop).roundToInt()
    val leaflets = tween(time, 2.15f, 2.9f, 0f, state.leaflets.toFloat(), pop).roundToInt()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(
            label = stringResource(R.string.finished_round_time),
            value = formatRoundTime(seconds),
            finalValue = formatRoundTime(state.seconds),
            enterAt = 1.9f,
            time = time,
            height = tileHeight,
            scale = scale,
            modifier = Modifier.fillMaxWidth(),
        )
        // Layout A (DEC-030): papers and leaflets side by side.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(stringResource(R.string.finished_newspapers), "$papers", "${state.newspapers}", 2.05f, time, tileHeight, scale, Modifier.weight(1f))
            StatTile(stringResource(R.string.finished_leaflets), "$leaflets", "${state.leaflets}", 2.05f, time, tileHeight, scale, Modifier.weight(1f))
        }
        val fill = tween(time, 2.8f, 3.2f, 0f, 1f, enter)
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)).background(Track)) {
            Box(Modifier.fillMaxWidth(fill).height(6.dp).background(Yellow))
        }
    }
}

/** A stat tile; TalkBack reads the final value, not the counting one. */
@Composable
private fun StatTile(
    label: String,
    value: String,
    finalValue: String,
    enterAt: Float,
    time: Float,
    height: Dp,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val p = tween(time, enterAt, enterAt + 0.55f, 0f, 1f, enter)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Card,
        border = BorderStroke(1.dp, Hairline),
        modifier = modifier
            .heightIn(min = height)
            .graphicsLayer {
                alpha = p
                translationY = (1 - p) * 60.dp.toPx()
            }
            .clearAndSetSemantics { contentDescription = "$label $finalValue" },
    ) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 14.dp)) {
            Text(
                label.uppercase(),
                style = TextStyle(fontFamily = Barlow, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.08.em, color = Grey),
            )
            Text(
                value,
                style = TextStyle(
                    fontFamily = BebasNeue,
                    fontStyle = FontStyle.Italic,
                    fontSize = (52 * scale).dpAsSp(),
                    lineHeight = (52 * scale).dpAsSp(),
                    color = Yellow,
                    fontFeatureSettings = "tnum",
                ),
            )
        }
    }
}

@Composable
private fun NextButton(time: Float, enabled: Boolean, onClick: () -> Unit, scale: Float) {
    val c = tween(time, 2.7f, 3.2f, 0f, 1f, enter)
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        color = Yellow,
        contentColor = Ink,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .graphicsLayer {
                alpha = c
                translationY = (1 - c) * 30.dp.toPx()
            },
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.finished_next).uppercase(),
                style = TextStyle(fontFamily = BebasNeue, fontStyle = FontStyle.Italic, fontSize = (34 * scale).dpAsSp(), letterSpacing = 0.04.em),
            )
            Spacer(Modifier.width(10.dp))
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}

/** Display sizes follow the spec's canvas, not the font-size setting, so the composition stays intact. */
@Composable
private fun Float.dpAsSp(): TextUnit = with(LocalDensity.current) { this@dpAsSp.dp.toSp() }
