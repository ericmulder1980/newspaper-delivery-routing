package nl.ericmulder.krantenwijk.ui.round

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.domain.model.DeliveryKind
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.repository.RoundRepository
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository
import nl.ericmulder.krantenwijk.domain.rules.DeliveryCounts
import nl.ericmulder.krantenwijk.domain.rules.FullRound
import nl.ericmulder.krantenwijk.domain.rules.deliveryKind
import nl.ericmulder.krantenwijk.domain.rules.deliverySummary
import nl.ericmulder.krantenwijk.ui.route.SegmentCell
import nl.ericmulder.krantenwijk.ui.route.StickerOption
import nl.ericmulder.krantenwijk.ui.route.applyStickerOption
import nl.ericmulder.krantenwijk.ui.route.cellsInWalkingOrder

sealed interface RoundStreetUiState {
    data object Loading : RoundStreetUiState

    /** The section was deleted while it was open. */
    data object Gone : RoundStreetUiState

    data class Ready(
        val segment: Segment,
        /** 1-based position and total, for "Street section 3 / 6". */
        val position: Int,
        val sectionCount: Int,
        val previous: Segment?,
        val next: Segment?,
        /** Houses and buildings to show, in walking order, after the visibility settings. */
        val cells: List<SegmentCell>,
        /** What this section needs, independent of what is shown. */
        val counts: DeliveryCounts,
        val showSkipped: Boolean,
        /** Houses hidden because they receive nothing and "Show skipped houses" is off. */
        val hiddenSkipped: Int,
        val keepScreenOn: Boolean,
    ) : RoundStreetUiState
}

/** One street section while delivering (RND-B, RND-04, RND-08, DEC-022). */
@HiltViewModel(assistedFactory = RoundStreetViewModel.Factory::class)
class RoundStreetViewModel @AssistedInject constructor(
    @Assisted private val segmentId: Long,
    private val routes: RouteRepository,
    private val settings: SettingsRepository,
    private val rounds: RoundRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(segmentId: Long): RoundStreetViewModel
    }

    val uiState: StateFlow<RoundStreetUiState> = combine(
        routes.observeSegmentContents(segmentId),
        routes.observeSegments(),
        settings.settings,
    ) { contents, segments, prefs ->
        if (contents == null) return@combine RoundStreetUiState.Gone
        val index = segments.indexOfFirst { it.id == segmentId }
        val standalone = contents.addresses.filter { it.buildingId == null }
        val all = cellsInWalkingOrder(contents.segment, standalone, contents.buildings, contents.addresses)
        val kindOf = { cell: SegmentCell -> (cell as? SegmentCell.House)?.let { deliveryKind(it.address) } }
        val cells = all.filter { cell ->
            when (kindOf(cell)) {
                DeliveryKind.NOTHING -> prefs.showSkipped
                DeliveryKind.DOES_NOT_EXIST -> prefs.showNonExisting
                else -> true
            }
        }
        RoundStreetUiState.Ready(
            segment = contents.segment,
            position = index + 1,
            sectionCount = segments.size,
            previous = segments.getOrNull(index - 1),
            next = segments.getOrNull(index + 1),
            cells = cells,
            counts = deliverySummary(contents.addresses, FullRound),
            showSkipped = prefs.showSkipped,
            hiddenSkipped = if (prefs.showSkipped) 0 else all.count { kindOf(it) == DeliveryKind.NOTHING },
            keepScreenOn = prefs.keepScreenOn,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoundStreetUiState.Loading)

    /** Whether leaving asks "Abandon this round?" (RND-13). */
    val roundActive: StateFlow<Boolean> = rounds.activeRound.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var finishing = false

    init {
        // Remembered for Resume after the app was closed (DEC-030).
        viewModelScope.launch { rounds.setCurrentSection(segmentId) }
    }

    fun abandonRound(then: () -> Unit) {
        viewModelScope.launch {
            rounds.abandonRound()
            then()
        }
    }

    /**
     * Saves the round with the route's totals at this moment (DEC-030), then calls [then] with its id
     * (null if no round was active, e.g. a round opened before this feature existed).
     */
    fun finishRound(then: (roundId: Long?) -> Unit) {
        if (finishing) return // a double tap must not finish twice
        finishing = true
        viewModelScope.launch {
            val totals = deliverySummary(routes.observeAllAddresses().first(), FullRound)
            then(rounds.finishRound(totals.newspapers, totals.leaflets))
        }
    }

    /** Quick correction at the mailbox (DEC-022): sticker or "does not exist", nothing else. */
    fun apply(option: StickerOption, addressIds: Collection<Long>) {
        viewModelScope.launch { routes.applyStickerOption(option, addressIds) }
    }

    fun setShowSkipped(show: Boolean) {
        viewModelScope.launch { settings.setShowSkipped(show) }
    }
}
