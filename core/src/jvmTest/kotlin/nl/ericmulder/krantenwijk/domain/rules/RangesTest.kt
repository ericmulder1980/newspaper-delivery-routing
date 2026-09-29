package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Side
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RangesTest {

    @Nested
    inner class Generate {
        @Test
        fun `2 to 24 even gives 12 even numbers`() {
            assertEquals((2..24 step 2).toList(), generateRange(2, 24, Side.EVEN))
        }

        @Test
        fun `1 to 23 odd gives 12 odd numbers`() {
            assertEquals((1..23 step 2).toList(), generateRange(1, 23, Side.ODD))
        }

        @Test
        fun `1 to 10 all gives every number`() {
            assertEquals((1..10).toList(), generateRange(1, 10, Side.ALL))
        }

        @Test
        fun `1 to 24 even starts at 2`() {
            assertEquals(2, generateRange(1, 24, Side.EVEN).first())
            assertEquals(12, generateRange(1, 24, Side.EVEN).size)
        }

        @Test
        fun `endpoint off the side is skipped at the top too`() {
            assertEquals(22, generateRange(2, 23, Side.EVEN).last())
        }

        @Test
        fun `from equals to gives a single number`() {
            assertEquals(listOf(7), generateRange(7, 7, Side.ALL))
            assertEquals(listOf(7), generateRange(7, 7, Side.ODD))
        }

        @Test
        fun `single number on the wrong side gives nothing`() {
            assertEquals(emptyList<Int>(), generateRange(7, 7, Side.EVEN))
        }

        @Test
        fun `rejects a range entered high to low`() {
            // DEC-012: ranges are entered low to high; reverse walking is a segment option.
            assertThrows<IllegalArgumentException> { generateRange(23, 1, Side.ODD) }
        }

        @Test
        fun `single-sided street with asymmetric numbering`() {
            // Evens go to 80, odds stop at 33 (plan §3.1).
            assertEquals(40, generateRange(2, 80, Side.EVEN).size)
            assertEquals(17, generateRange(1, 33, Side.ODD).size)
        }

        @Test
        fun `rejects non-positive numbers`() {
            assertThrows<IllegalArgumentException> { generateRange(0, 10, Side.ALL) }
            assertThrows<IllegalArgumentException> { generateRange(5, -1, Side.ALL) }
        }

        @Test
        fun `rejects more than the maximum`() {
            assertEquals(MAX_RANGE_SIZE, generateRange(1, MAX_RANGE_SIZE, Side.ALL).size)
            assertThrows<IllegalArgumentException> { generateRange(1, MAX_RANGE_SIZE + 1, Side.ALL) }
        }
    }

    @Nested
    inner class Count {
        @Test
        fun `count matches generated size`() {
            for (side in Side.entries) {
                for (from in 1..12) {
                    for (to in 1..12) {
                        val expected = (from..to).count(side::matches)
                        assertEquals(expected, countInRange(from, to, side), "$from → $to $side")
                    }
                }
            }
        }
    }

    @Nested
    inner class Check {
        @Test
        fun `valid range has no issues`() {
            assertEquals(emptyList<RangeIssue>(), checkRange(2, 24, Side.EVEN))
            assertEquals(emptyList<RangeIssue>(), checkRange(1, 23, Side.ODD))
        }

        @Test
        fun `high to low blocks`() {
            val issues = checkRange(23, 1, Side.ODD)
            assertEquals(listOf(RangeIssue.FromAfterTo), issues)
            assertTrue(issues.all { it.isBlocking })
        }

        @Test
        fun `missing or non-positive number is invalid`() {
            assertEquals(listOf(RangeIssue.InvalidNumber), checkRange(null, 24, Side.EVEN))
            assertEquals(listOf(RangeIssue.InvalidNumber), checkRange(0, 24, Side.EVEN))
        }

        @Test
        fun `odd endpoint on even side warns but does not block`() {
            val issues = checkRange(2, 23, Side.EVEN)
            assertEquals(listOf(RangeIssue.EndpointNotOnSide(Side.EVEN)), issues)
            assertTrue(issues.none { it.isBlocking })
        }

        @Test
        fun `empty range blocks`() {
            val issues = checkRange(3, 3, Side.EVEN)
            assertTrue(RangeIssue.NoNumbers in issues)
            assertTrue(issues.any { it.isBlocking })
        }

        @Test
        fun `too many numbers blocks`() {
            assertEquals(listOf(RangeIssue.TooManyNumbers(301)), checkRange(1, 301, Side.ALL))
        }
    }
}
