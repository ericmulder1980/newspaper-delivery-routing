package nl.ericmulder.krantenwijk.ui.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import nl.ericmulder.krantenwijk.domain.model.ActiveRound
import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import nl.ericmulder.krantenwijk.domain.repository.RoundRepository

/** In-memory [RoundRepository] with a settable clock. */
class FakeRoundRepository(var now: Long = 1_000) : RoundRepository {
    override val activeRound = MutableStateFlow<ActiveRound?>(null)
    val rounds = MutableStateFlow<List<CompletedRound>>(emptyList())

    override suspend fun startRound(segmentId: Long) =
        activeRound.update { it?.copy(currentSegmentId = segmentId) ?: ActiveRound(now, segmentId) }

    override suspend fun setCurrentSection(segmentId: Long) = activeRound.update { it?.copy(currentSegmentId = segmentId) }

    override suspend fun abandonRound() {
        activeRound.value = null
    }

    override suspend fun finishRound(newspapers: Int, leaflets: Int): Long? {
        val active = activeRound.value ?: return null
        val id = (rounds.value.maxOfOrNull { it.id } ?: 0) + 1
        rounds.update { it + CompletedRound(id, active.startedAtMillis, now, newspapers, leaflets) }
        activeRound.value = null
        return id
    }

    override fun observeRound(id: Long): Flow<CompletedRound?> = rounds.map { list -> list.firstOrNull { it.id == id } }

    override fun observeCompletedRounds(): Flow<List<CompletedRound>> = rounds

    override suspend fun deleteRounds(ids: Collection<Long>) = rounds.update { list -> list.filterNot { it.id in ids } }

    override suspend fun restoreRounds(rounds: List<CompletedRound>) =
        this.rounds.update { list -> list + rounds.filter { r -> list.none { it.id == r.id } } }
}
