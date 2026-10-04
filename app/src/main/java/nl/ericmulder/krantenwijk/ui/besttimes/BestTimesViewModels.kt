package nl.ericmulder.krantenwijk.ui.besttimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import nl.ericmulder.krantenwijk.domain.repository.RoundRepository
import nl.ericmulder.krantenwijk.domain.rules.rankedByDuration
import javax.inject.Inject

/** A finished round with its 1-based place, fastest first (DEC-030). */
data class RankedRound(val rank: Int, val round: CompletedRound)

private fun List<CompletedRound>.ranked() = rankedByDuration(this).mapIndexed { index, round -> RankedRound(index + 1, round) }

data class Top5UiState(
    val top: List<RankedRound>,
    /** The round just finished; highlighted when it's in [top]. */
    val newRoundId: Long,
    /** The round just finished when it didn't make the top 5, shown below with its place. */
    val newOutsideTop: RankedRound?,
)

/** Top 5 after the Finished screen (RND-14). */
@HiltViewModel(assistedFactory = Top5ViewModel.Factory::class)
class Top5ViewModel @AssistedInject constructor(
    @Assisted private val roundId: Long,
    rounds: RoundRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(roundId: Long): Top5ViewModel
    }

    val uiState: StateFlow<Top5UiState?> = rounds.observeCompletedRounds().map { all ->
        val ranked = all.ranked()
        Top5UiState(
            top = ranked.take(TOP),
            newRoundId = roundId,
            newOutsideTop = ranked.firstOrNull { it.round.id == roundId && it.rank > TOP },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private companion object {
        const val TOP = 5
    }
}

data class BestTimesUiState(val rows: List<RankedRound>, val selected: Set<Long>) {
    val selecting: Boolean get() = selected.isNotEmpty()
}

/** Settings › Best times: every finished round, ranked; select and delete with Undo (RND-14). */
@HiltViewModel
class BestTimesViewModel @Inject constructor(private val rounds: RoundRepository) : ViewModel() {

    private val selected = MutableStateFlow<Set<Long>>(emptySet())
    private var lastDeleted: List<CompletedRound> = emptyList()

    val uiState: StateFlow<BestTimesUiState?> = combine(rounds.observeCompletedRounds(), selected) { all, selection ->
        val ranked = all.ranked()
        // A selected round that no longer exists is dropped from the selection.
        BestTimesUiState(ranked, selection intersect ranked.map { it.round.id }.toSet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Long-press starts selecting; while selecting, a tap adds or removes a round. */
    fun toggle(roundId: Long) {
        selected.value = selected.value.let { if (roundId in it) it - roundId else it + roundId }
    }

    fun clearSelection() {
        selected.value = emptySet()
    }

    /** Deletes the selected rounds and returns how many, for the "n times deleted · Undo" message. */
    fun deleteSelected(): Int {
        val state = uiState.value ?: return 0
        val gone = state.rows.map { it.round }.filter { it.id in state.selected }
        if (gone.isEmpty()) return 0
        lastDeleted = gone
        selected.value = emptySet()
        viewModelScope.launch { rounds.deleteRounds(gone.map { it.id }) }
        return gone.size
    }

    fun undoDelete() {
        val restore = lastDeleted
        lastDeleted = emptyList()
        if (restore.isNotEmpty()) viewModelScope.launch { rounds.restoreRounds(restore) }
    }
}
