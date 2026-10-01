package nl.ericmulder.krantenwijk.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository
import javax.inject.Inject

data class OnboardingUiState(
    val nickname: String?,
    val route: Route?,
    val segments: List<Segment>,
    /** Existing addresses per segment id. */
    val addressCounts: Map<Long, Int>,
)

/** First-launch setup (ADR-01): name → route → street sections → walking route. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val routes: RouteRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<OnboardingUiState?> = combine(
        settings.settings.map { it.nickname },
        routes.observeRoute(),
        routes.observeSegments(),
        routes.observeAddressCounts(),
    ) { nickname, route, segments, counts ->
        OnboardingUiState(nickname, route, segments, counts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Step 1. Returns false (and saves nothing) if the name is blank. */
    fun saveName(name: String, onSaved: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            settings.setNickname(name)
            onSaved()
        }
    }

    /** Step 2: creates the route or updates it when the user came back to this step. */
    fun saveRoute(name: String, town: String, onSaved: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            routes.saveRoute(name, town)
            onSaved()
        }
    }

    /** Step 4: the walking order (ADR-05). */
    fun reorder(segmentIds: List<Long>) {
        viewModelScope.launch { routes.reorderSegments(segmentIds) }
    }

    /** Step 4 "Done": setup is complete; the app opens on the home screen from now on. */
    fun finish(onFinished: () -> Unit) {
        viewModelScope.launch {
            settings.setOnboardingCompleted(true)
            onFinished()
        }
    }
}

/**
 * Where the app opens: the setup wizard only on a real first launch. Someone who already has a
 * route (e.g. created before the wizard existed) goes straight home.
 */
fun needsOnboarding(onboardingCompleted: Boolean, hasRoute: Boolean): Boolean = !onboardingCompleted && !hasRoute
