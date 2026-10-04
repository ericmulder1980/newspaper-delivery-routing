package nl.ericmulder.krantenwijk.domain.model

/**
 * The round being walked (DEC-030). Only its start and where the deliverer is are kept, so the timer
 * survives a force-close or phone restart. [currentSegmentId] is null until a section has been opened.
 */
data class ActiveRound(val startedAtMillis: Long, val currentSegmentId: Long?)

/** A finished round (DEC-030). Counts are the route's totals at the moment of finishing (no check-offs). */
data class CompletedRound(
    val id: Long,
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
    val newspapers: Int,
    val leaflets: Int,
) {
    /** Never negative, even if the phone's clock was set back during the round. */
    val durationMillis: Long get() = (finishedAtMillis - startedAtMillis).coerceAtLeast(0)
}
