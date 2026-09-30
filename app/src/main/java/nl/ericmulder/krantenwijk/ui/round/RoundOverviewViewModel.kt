package nl.ericmulder.krantenwijk.ui.round

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.rules.DeliveryCounts
import nl.ericmulder.krantenwijk.domain.rules.FullRound
import nl.ericmulder.krantenwijk.domain.rules.deliverySummary
import javax.inject.Inject

/** One section in the walking route with what it needs. */
data class SectionTotals(val segment: Segment, val counts: DeliveryCounts)

sealed interface RoundOverviewUiState {
    data object Loading : RoundOverviewUiState

    data class Ready(
        val routeName: String?,
        val sections: List<SectionTotals>,
        /** Newspapers and leaflets to take along for the whole route (RND-02, DEC-019). */
        val totals: DeliveryCounts,
    ) : RoundOverviewUiState
}

/** Walking route overview: totals and all sections in walking order (RND-A, DEC-022). */
@HiltViewModel
class RoundOverviewViewModel @Inject constructor(routes: RouteRepository) : ViewModel() {

    val uiState: StateFlow<RoundOverviewUiState> = combine(
        routes.observeRoute(),
        routes.observeSegments(),
        routes.observeAllAddresses(),
    ) { route, segments, addresses ->
        val bySegment = addresses.groupBy { it.segmentId }
        RoundOverviewUiState.Ready(
            routeName = route?.name,
            sections = segments.map { SectionTotals(it, deliverySummary(bySegment[it.id].orEmpty(), FullRound)) },
            totals = deliverySummary(addresses, FullRound),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoundOverviewUiState.Loading)
}
