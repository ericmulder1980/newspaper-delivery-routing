package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.CompletedRound

/** Fastest first; on a tie the earlier round ranks higher (DEC-030). */
fun rankedByDuration(rounds: List<CompletedRound>): List<CompletedRound> =
    rounds.sortedWith(compareBy<CompletedRound> { it.durationMillis }.thenBy { it.startedAtMillis }.thenBy { it.id })

/** 1-based rank of round [id] among [rounds], or null if it isn't there. */
fun rankOf(id: Long, rounds: List<CompletedRound>): Int? =
    rankedByDuration(rounds).indexOfFirst { it.id == id }.takeIf { it >= 0 }?.plus(1)

/** Whole seconds, rounded to the nearest second. */
fun roundSeconds(durationMillis: Long): Long = (durationMillis.coerceAtLeast(0) + 500) / 1000

/** "h:mm:ss", hours unpadded: 0:45:07, 1:12:15, 10:00:00 (DEC-030). */
fun formatRoundTime(seconds: Long): String {
    val s = seconds.coerceAtLeast(0)
    val hours = s / 3600
    val minutes = (s % 3600) / 60
    val secs = s % 60
    return "$hours:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
