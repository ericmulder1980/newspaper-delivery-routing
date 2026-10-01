package nl.ericmulder.krantenwijk.ui.route

import android.app.Application
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.down
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.up
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** ADR-05: sections can be reordered by dragging the handle, and with TalkBack's Move up / Move down. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class ReorderableSectionsTest {

    @get:Rule
    val compose = createComposeRule()

    private val segments = listOf(
        Segment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING, 0, id = 1),
        Segment("Molenweg", Side.ALL, 1, 10, Direction.ASCENDING, 1, id = 2),
        Segment("Lindelaan", Side.EVEN, 2, 16, Direction.ASCENDING, 2, id = 3),
    )

    private val reorders = mutableListOf<List<Long>>()

    private fun show() = compose.setContent {
        KrantenwijkTheme(dark = true) {
            ReorderableSections(segments = segments, detail = { "x" }, onOpen = {}, onReorder = { reorders += it })
        }
    }

    /** Runs the TalkBack custom action [label] on the row that offers exactly [labels]. */
    private fun runAction(label: String, labels: Set<String>) {
        val node = compose.onNode(
            SemanticsMatcher("row with $labels") { n ->
                n.config.getOrNull(SemanticsActions.CustomActions)?.map { it.label }?.toSet() == labels
            },
        ).fetchSemanticsNode()
        compose.runOnIdle { node.config[SemanticsActions.CustomActions].first { it.label == label }.action() }
        compose.waitForIdle()
    }

    @Test
    fun `move down via accessibility action`() {
        show()
        // The first row only offers Move down; the last only Move up.
        runAction("Move down", setOf("Move down"))
        assertEquals(listOf(listOf(2L, 1L, 3L)), reorders)
    }

    @Test
    fun `move up via accessibility action`() {
        show()
        runAction("Move up", setOf("Move up"))
        assertEquals(listOf(listOf(1L, 3L, 2L)), reorders)
    }

    @Test
    fun `dragging the handle to the bottom moves a section and saves once`() {
        show()
        compose.onNodeWithContentDescription("Drag to move Kerkstraat").performTouchInput {
            down(center)
            // Well past the last row (rows are at least 72 dp high plus 8 dp spacing).
            repeat(40) { moveBy(Offset(0f, 10.dp.toPx())) }
            up()
        }
        compose.waitForIdle()
        assertEquals(1, reorders.size)
        assertEquals(listOf(2L, 3L, 1L), reorders.single())
    }
}
