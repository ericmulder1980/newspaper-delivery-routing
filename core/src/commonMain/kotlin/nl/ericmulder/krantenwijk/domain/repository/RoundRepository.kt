package nl.ericmulder.krantenwijk.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.ericmulder.krantenwijk.domain.model.ActiveRound
import nl.ericmulder.krantenwijk.domain.model.CompletedRound

/** The active round and finished rounds (DEC-030). Every write is committed immediately (NFR-05). */
interface RoundRepository {
    /** The round being walked, or null when none is. */
    val activeRound: Flow<ActiveRound?>

    /** Starts the timer now at [segmentId]. If a round is already active, only the section is updated. */
    suspend fun startRound(segmentId: Long)

    /** Remembers where the deliverer is, for Resume. Does nothing when no round is active. */
    suspend fun setCurrentSection(segmentId: Long)

    /** Discards the active round; nothing is saved. */
    suspend fun abandonRound()

    /**
     * Saves the active round as finished now, with the route's totals, and clears it.
     * Returns the saved round's id, or null when no round was active. Finishing the same round
     * twice (e.g. after a crash between the two writes) saves it once.
     */
    suspend fun finishRound(newspapers: Int, leaflets: Int): Long?

    fun observeRound(id: Long): Flow<CompletedRound?>

    fun observeCompletedRounds(): Flow<List<CompletedRound>>
}
