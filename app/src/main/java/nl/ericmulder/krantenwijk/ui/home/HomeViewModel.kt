package nl.ericmulder.krantenwijk.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(routeRepository: RouteRepository) : ViewModel() {
    /** The route's name, or null while there is no route yet. */
    val routeName: StateFlow<String?> = routeRepository.observeRoute()
        .map { it?.name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)
}
