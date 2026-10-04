package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RoundsTest {

    private fun round(id: Long, start: Long, minutes: Long) =
        CompletedRound(id, startedAtMillis = start, finishedAtMillis = start + minutes * 60_000, newspapers = 57, leaflets = 41)

    @Test
    fun `fastest first, ties go to the earlier round (DEC-030)`() {
        val slow = round(1, start = 0, minutes = 80)
        val laterTie = round(2, start = 2_000_000, minutes = 70)
        val fast = round(3, start = 1_000_000, minutes = 65)
        val earlierTie = round(4, start = 1_500_000, minutes = 70)
        assertEquals(listOf(fast, earlierTie, laterTie, slow), rankedByDuration(listOf(slow, laterTie, fast, earlierTie)))
    }

    @Test
    fun `rank is 1-based, null when missing`() {
        val rounds = listOf(round(1, 0, 80), round(2, 10, 60))
        assertEquals(1, rankOf(2, rounds))
        assertEquals(2, rankOf(1, rounds))
        assertNull(rankOf(9, rounds))
    }

    @Test
    fun `duration is never negative`() {
        assertEquals(0, CompletedRound(1, startedAtMillis = 5_000, finishedAtMillis = 1_000, newspapers = 0, leaflets = 0).durationMillis)
    }

    @Test
    fun `seconds round to the nearest second`() {
        assertEquals(0, roundSeconds(499))
        assertEquals(1, roundSeconds(500))
        assertEquals(4335, roundSeconds(4_335_400))
        assertEquals(0, roundSeconds(-10))
    }

    @Test
    fun `time is h-mm-ss`() {
        assertEquals("0:00:00", formatRoundTime(0))
        assertEquals("0:45:07", formatRoundTime(45 * 60 + 7))
        assertEquals("1:12:15", formatRoundTime(4335))
        assertEquals("10:00:00", formatRoundTime(36_000))
    }
}
