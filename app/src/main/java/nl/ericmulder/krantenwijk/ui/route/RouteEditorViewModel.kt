package nl.ericmulder.krantenwijk.ui.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import javax.inject.Inject

sealed interface RouteEditorUiState {
    data object Loading : RouteEditorUiState

    /** No route yet: ask for its name first (becomes step 2 of the setup wizard in ONB-A). */
    data object NoRoute : RouteEditorUiState

    /** [addressCounts]: existing addresses per segment id (ADR-04). */
    data class Ready(val route: Route, val segments: List<Segment>, val addressCounts: Map<Long, Int>) : RouteEditorUiState
}

@HiltViewModel
class RouteEditorViewModel @Inject constructor(private val routes: RouteRepository) : ViewModel() {

    val uiState: StateFlow<RouteEditorUiState> =
        combine(routes.observeRoute(), routes.observeSegments(), routes.observeAddressCounts()) { route, segments, counts ->
            if (route == null) RouteEditorUiState.NoRoute else RouteEditorUiState.Ready(route, segments, counts)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RouteEditorUiState.Loading)

    /** New walking order after drag-and-drop (ADR-05); [segmentIds] lists every section once. */
    fun reorder(segmentIds: List<Long>) {
        viewModelScope.launch { routes.reorderSegments(segmentIds) }
    }

    fun createRoute(name: String, town: String) {
        if (name.isBlank()) return
        viewModelScope.launch { routes.saveRoute(name, town) }
    }
}
