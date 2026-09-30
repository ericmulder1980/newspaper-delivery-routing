package nl.ericmulder.krantenwijk.ui

import android.app.Application
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.ui.common.PrimaryActionHeight
import nl.ericmulder.krantenwijk.ui.common.TargetSpacing
import nl.ericmulder.krantenwijk.ui.common.WalkTouchTarget
import nl.ericmulder.krantenwijk.ui.round.RoundStreetScreen
import nl.ericmulder.krantenwijk.ui.round.RoundStreetViewModel
import nl.ericmulder.krantenwijk.ui.route.BuildingDetailScreen
import nl.ericmulder.krantenwijk.ui.route.BuildingDetailViewModel
import nl.ericmulder.krantenwijk.ui.testing.FakeRouteRepository
import nl.ericmulder.krantenwijk.ui.testing.FakeSettingsRepository
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * DEC-023 on a small phone (360 dp wide, the narrowest in plan NFR-07): things tapped while walking
 * are at least 64×64 dp and at least 8 dp apart; Previous/Next are at least 72 dp high.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, qualifiers = "w360dp-h640dp-port")
class TouchTargetTest {

    @get:Rule
    val compose = createComposeRule()

    private val repo = FakeRouteRepository()
    private val segmentId = runBlocking {
        repo.saveRoute("Wijk 07", null)
        repo.addSegment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING).also {
            repo.addSegment("Molenweg", Side.ALL, 1, 10, Direction.ASCENDING)
        }
    }

    private fun assertAtLeast(name: String, bounds: DpRect, minWidth: Dp, minHeight: Dp) {
        val w = bounds.right - bounds.left
        val h = bounds.bottom - bounds.top
        assertTrue("$name is $w × $h, expected at least $minWidth × $minHeight", w >= minWidth && h >= minHeight)
    }

    private fun assertGap(name: String, gap: Dp) {
        assertTrue("$name gap is $gap, expected at least $TargetSpacing", gap >= TargetSpacing - 0.5.dp)
    }

    @Test
    fun `mailboxes are big enough and far enough apart`() {
        val buildingId = runBlocking { repo.createBuilding(segmentId, 12, SuffixType.LETTER, "A", "Z") }
        val vm = BuildingDetailViewModel(buildingId, repo)
        compose.setContent { KrantenwijkTheme(dark = true) { BuildingDetailScreen(buildingId, onBack = {}, viewModel = vm) } }
        compose.waitForIdle()

        val a = compose.onNodeWithContentDescription("12A, No sticker, Newspaper + leaflets").getBoundsInRoot()
        val b = compose.onNodeWithContentDescription("12B, No sticker, Newspaper + leaflets").getBoundsInRoot()
        val e = compose.onNodeWithContentDescription("12E, No sticker, Newspaper + leaflets").getBoundsInRoot()
        assertAtLeast("Mailbox 12A", a, WalkTouchTarget, WalkTouchTarget)
        assertGap("12A–12B (side by side)", b.left - a.right)
        assertGap("12A–12E (below)", e.top - a.bottom)
    }

    @Test
    fun `round tiles and previous-next buttons are big enough`() {
        val vm = RoundStreetViewModel(segmentId, repo, FakeSettingsRepository())
        compose.setContent {
            KrantenwijkTheme(dark = true) {
                RoundStreetScreen(segmentId, onBack = {}, onGoTo = {}, onFinish = {}, onOpenBuilding = {}, viewModel = vm)
            }
        }
        compose.waitForIdle()

        val two = compose.onNodeWithContentDescription("2, No sticker, Newspaper + leaflets").getBoundsInRoot()
        val four = compose.onNodeWithContentDescription("4, No sticker, Newspaper + leaflets").getBoundsInRoot()
        val ten = compose.onNodeWithContentDescription("10, No sticker, Newspaper + leaflets").getBoundsInRoot()
        assertAtLeast("Tile 2", two, WalkTouchTarget, WalkTouchTarget)
        assertGap("2–4 (side by side)", four.left - two.right)
        assertGap("2–10 (below)", ten.top - two.bottom)

        val next = compose.onNode(hasText("Next")).getBoundsInRoot()
        val previous = compose.onNode(hasText("Previous")).getBoundsInRoot()
        assertAtLeast("Next", next, WalkTouchTarget, PrimaryActionHeight)
        assertAtLeast("Previous", previous, WalkTouchTarget, PrimaryActionHeight)
        assertGap("Previous–Next", next.left - previous.right)
    }
}
