package nl.ericmulder.krantenwijk.ui.route

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.foundation.layout.Column
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Plan §10: the delivery states render distinct, and never by colour alone (NFR-04): every tile
 * carries its sticker as text and its delivery in the spoken label.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class DeliveryTileTest {

    @get:Rule
    val compose = createComposeRule()

    private val tiles = listOf(
        Address(2, sticker = Sticker.NONE, id = 1),
        Address(4, sticker = Sticker.NEE_JA, id = 2),
        Address(6, sticker = Sticker.NEE_NEE, id = 3),
        Address(8, exists = false, id = 4),
    )

    private fun showTiles(dark: Boolean = true) = compose.setContent {
        KrantenwijkTheme(dark = dark) {
            Column {
                tiles.forEach { StickerTile(it, it.houseLabel(), it.houseLabel(), selected = null, onClick = {}) }
            }
        }
    }

    @Test
    fun `each state has its own spoken label`() {
        showTiles()
        compose.onNodeWithContentDescription("2, No sticker, Newspaper + leaflets").assertExists()
        compose.onNodeWithContentDescription("4, NEE/JA sticker, Newspaper only").assertExists()
        compose.onNodeWithContentDescription("6, NEE/NEE sticker, Nothing").assertExists()
        compose.onNodeWithContentDescription("8, Does not exist, Does not exist").assertExists()
    }

    private fun assertStickerTexts() {
        compose.onNodeWithText("None", useUnmergedTree = true).assertExists()
        compose.onNodeWithText("NEE/JA", useUnmergedTree = true).assertExists()
        compose.onNodeWithText("NEE/NEE", useUnmergedTree = true).assertExists()
        compose.onAllNodesWithText("Does not exist", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun `stickers are shown as text in the dark theme`() {
        showTiles(dark = true)
        assertStickerTexts()
    }

    @Test
    fun `stickers are shown as text in the light theme`() {
        showTiles(dark = false)
        assertStickerTexts()
    }

    @Test
    @Config(qualifiers = "nl")
    fun `labels are Dutch on a Dutch device (LANG-01)`() {
        showTiles()
        compose.onNodeWithContentDescription("4, NEE/JA-sticker, Alleen krant").assertExists()
        compose.onNodeWithContentDescription("8, Bestaat niet, Bestaat niet").assertExists()
    }

    @Test
    @Config(qualifiers = "de")
    fun `unsupported language falls back to English (LANG-01)`() {
        showTiles()
        compose.onNodeWithContentDescription("6, NEE/NEE sticker, Nothing").assertExists()
    }
}
