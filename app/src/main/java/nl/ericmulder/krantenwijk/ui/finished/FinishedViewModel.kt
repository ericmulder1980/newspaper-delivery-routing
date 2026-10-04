package nl.ericmulder.krantenwijk.ui.finished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import nl.ericmulder.krantenwijk.domain.repository.RoundRepository
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository
import nl.ericmulder.krantenwijk.domain.rules.roundSeconds

/** What the Finished screen shows (DEC-030): the deliverer's name, the round time and the counts. */
data class FinishedUiState(val name: String?, val seconds: Long, val newspapers: Int, val leaflets: Int)

/** The end-of-round screen (RND-13). Reads the saved round, so it survives process death. */
@HiltViewModel(assistedFactory = FinishedViewModel.Factory::class)
class FinishedViewModel @AssistedInject constructor(
    @Assisted roundId: Long,
    rounds: RoundRepository,
    settings: SettingsRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(roundId: Long): FinishedViewModel
    }

    /** Null while loading, or if the round no longer exists. */
    val uiState: StateFlow<FinishedUiState?> = combine(rounds.observeRound(roundId), settings.settings) { round, prefs ->
        round?.let { FinishedUiState(prefs.nickname, roundSeconds(it.durationMillis), it.newspapers, it.leaflets) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
