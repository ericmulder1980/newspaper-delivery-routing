package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SectionPreviewTest {

    @Nested
    inner class Preview {
        @Test
        fun `2 to 24 even previews 12 addresses`() {
            val preview = previewSection(2, 24, Side.EVEN, Direction.ASCENDING)
            assertEquals((2..24 step 2).toList(), preview.numbers)
            assertTrue(preview.canSave)
        }

        @Test
        fun `reverse walking lists numbers high to low`() {
            val preview = previewSection(1, 23, Side.ODD, Direction.DESCENDING)
            assertEquals((1..23 step 2).toList().reversed(), preview.numbers)
            assertTrue(preview.canSave)
        }

        @Test
        fun `endpoint on the wrong side warns but can still be saved`() {
            val preview = previewSection(2, 23, Side.EVEN, Direction.ASCENDING)
            assertEquals(listOf(RangeIssue.EndpointNotOnSide(Side.EVEN)), preview.issues)
            assertEquals(11, preview.numbers.size)
            assertTrue(preview.canSave)
        }

        @Test
        fun `missing number cannot be saved`() {
            val preview = previewSection(2, null, Side.EVEN, Direction.ASCENDING)
            assertEquals(emptyList<Int>(), preview.numbers)
            assertFalse(preview.canSave)
        }

        @Test
        fun `high to low cannot be saved`() {
            val preview = previewSection(24, 2, Side.EVEN, Direction.ASCENDING)
            assertEquals(listOf(RangeIssue.FromAfterTo), preview.issues)
            assertFalse(preview.canSave)
        }

        @Test
        fun `too many numbers cannot be saved`() {
            assertFalse(previewSection(1, 400, Side.ALL, Direction.ASCENDING).canSave)
        }

        @Test
        fun `empty range cannot be saved`() {
            assertFalse(previewSection(3, 3, Side.EVEN, Direction.ASCENDING).canSave)
        }
    }

    @Nested
    inner class Sample {
        @Test
        fun `short lists are shown in full`() {
            assertEquals(listOf(2, 4, 6, 8, 10), previewSample(listOf(2, 4, 6, 8, 10)))
        }

        @Test
        fun `long lists show the first three and the last`() {
            assertEquals(listOf(2, 4, 6, null, 24), previewSample((2..24 step 2).toList()))
        }
    }

    @Nested
    inner class Suggestions {
        private val used = listOf("Kerkstraat", "Kerkpad", "Molenweg", "kerkstraat")

        @Test
        fun `prefix match ignoring case`() {
            assertEquals(listOf("Kerkstraat", "Kerkpad"), suggestStreetNames(used, "ker"))
        }

        @Test
        fun `exact match is not suggested`() {
            assertEquals(emptyList<String>(), suggestStreetNames(used, "Molenweg"))
        }

        @Test
        fun `blank query suggests nothing`() {
            assertEquals(emptyList<String>(), suggestStreetNames(used, "  "))
        }

        @Test
        fun `suggestions are limited`() {
            val many = (1..10).map { "Laan $it" }
            assertEquals(5, suggestStreetNames(many, "laan").size)
        }
    }
}
