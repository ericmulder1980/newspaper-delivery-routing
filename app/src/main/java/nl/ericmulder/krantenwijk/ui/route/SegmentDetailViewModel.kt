package nl.ericmulder.krantenwijk.ui.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Building
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.domain.repository.BuildingConflictException
import nl.ericmulder.krantenwijk.domain.repository.DuplicateAddressException
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.rules.AddressNumberOrder
import nl.ericmulder.krantenwijk.domain.rules.DeliveryCounts
import nl.ericmulder.krantenwijk.domain.rules.FullRound
import nl.ericmulder.krantenwijk.domain.rules.StickerCounts
import nl.ericmulder.krantenwijk.domain.rules.deliverySummary
import nl.ericmulder.krantenwijk.domain.rules.stickerSummary
import nl.ericmulder.krantenwijk.domain.rules.unitsOrNull

/** A choice in the "Sticker on mailbox" sheet: one of the four stickers, or "does not exist" (as in the prototype). */
sealed interface StickerOption {
    data class Set(val sticker: Sticker) : StickerOption

    data object DoesNotExist : StickerOption

    companion object {
        val all: List<StickerOption> = Sticker.entries.map(::Set) + DoesNotExist
    }
}

/** The option that matches [address] as it is now ("Current" in the sheet). */
fun Address.currentOption(): StickerOption = if (!exists) StickerOption.DoesNotExist else StickerOption.Set(sticker)

/** A building with its apartments and their totals, for the collapsed row (BLD-02). */
data class BuildingSummary(
    val building: Building,
    /** Apartments in walking order, including "does not exist" ones. */
    val apartments: List<Address>,
    /** Sticker counts over existing apartments. */
    val stickers: StickerCounts,
    /** Deliveries over existing apartments for a full round. */
    val counts: DeliveryCounts,
) {
    val existingCount: Int get() = apartments.count { it.exists }
}

/** One item in the street's number grid, in walking order. */
sealed interface SegmentCell {
    data class House(val address: Address) : SegmentCell

    data class Apartments(val summary: BuildingSummary) : SegmentCell
}

sealed interface SegmentDetailUiState {
    data object Loading : SegmentDetailUiState

    /** The segment no longer exists (deleted here or elsewhere). */
    data object Gone : SegmentDetailUiState

    data class Ready(
        val segment: Segment,
        /** 1-based place in the walking order. */
        val position: Int,
        /** Standalone addresses in walking order (apartments are inside [cells]). */
        val addresses: List<Address>,
        /** Houses and buildings in walking order; a building takes the place of its number. */
        val cells: List<SegmentCell>,
        /** Addresses that exist; "does not exist" is not counted (ADR-04). */
        val existingCount: Int,
        /** Newspapers and leaflets for a full round (STK-03). */
        val counts: DeliveryCounts,
        /** Non-null while selecting numbers for a bulk change (STK-02). */
        val selection: Set<Long>?,
    ) : SegmentDetailUiState {
        val selecting: Boolean get() = selection != null
    }
}

/** Why a building couldn't be created, shown in the building sheet. */
sealed interface BuildingError {
    data object InvalidRange : BuildingError

    /** Standalone numbers that already use unit labels, e.g. ["12C"]. */
    data class Conflict(val labels: List<String>) : BuildingError
}

/** Result of trying to add a number, shown in the add sheet. */
sealed interface AddNumberError {
    data object InvalidNumber : AddNumberError

    data class Duplicate(val label: String) : AddNumberError
}

/** One street section: numbers (ADR-12, ADR-03, ADR-04) and stickers, one by one or in bulk (STK-01..03). */
@HiltViewModel(assistedFactory = SegmentDetailViewModel.Factory::class)
class SegmentDetailViewModel @AssistedInject constructor(
    @Assisted private val segmentId: Long,
    private val routes: RouteRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(segmentId: Long): SegmentDetailViewModel
    }

    private val selection = MutableStateFlow<Set<Long>?>(null)

    val uiState: StateFlow<SegmentDetailUiState> = combine(
        routes.observeSegmentContents(segmentId),
        routes.observeSegments(),
        selection,
    ) { contents, segments, selected ->
        if (contents == null) {
            SegmentDetailUiState.Gone
        } else {
            val standalone = contents.addresses.filter { it.buildingId == null }
            val ids = standalone.mapTo(HashSet()) { it.id }
            SegmentDetailUiState.Ready(
                segment = contents.segment,
                position = segments.indexOfFirst { it.id == segmentId } + 1,
                addresses = standalone,
                cells = cellsInWalkingOrder(contents.segment, standalone, contents.buildings, contents.addresses),
                existingCount = contents.addresses.count { it.exists },
                counts = deliverySummary(contents.addresses, FullRound),
                // Numbers deleted meanwhile drop out of the selection.
                selection = selected?.intersect(ids),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SegmentDetailUiState.Loading)

    private val _buildingError = MutableStateFlow<BuildingError?>(null)
    val buildingError: StateFlow<BuildingError?> = _buildingError.asStateFlow()

    private val _buildingCreated = MutableStateFlow(0)

    /** Increments after each created building, so the screen can close the building sheet. */
    val buildingCreated: StateFlow<Int> = _buildingCreated.asStateFlow()

    private val _addError = MutableStateFlow<AddNumberError?>(null)
    val addError: StateFlow<AddNumberError?> = _addError.asStateFlow()

    private val _added = MutableStateFlow(0)

    /** Increments after each successful add, so the screen can close the add sheet. */
    val added: StateFlow<Int> = _added.asStateFlow()

    fun startSelecting() {
        selection.value = emptySet()
    }

    fun stopSelecting() {
        selection.value = null
    }

    fun toggleSelected(address: Address) = selection.update { current ->
        current?.let { if (address.id in it) it - address.id else it + address.id }
    }

    fun selectAll() {
        val ready = uiState.value as? SegmentDetailUiState.Ready ?: return
        selection.value = ready.addresses.mapTo(HashSet()) { it.id }
    }

    /**
     * Applies [option] to [addressIds]: a sticker also marks them as existing again; "does not exist"
     * keeps the sticker so it comes back when the number is restored.
     */
    fun apply(option: StickerOption, addressIds: Collection<Long>) {
        if (addressIds.isEmpty()) return
        viewModelScope.launch {
            when (option) {
                is StickerOption.Set -> {
                    routes.setSticker(addressIds, option.sticker)
                    routes.setExists(addressIds, true)
                }
                StickerOption.DoesNotExist -> routes.setExists(addressIds, false)
            }
            stopSelecting()
        }
    }

    fun delete(addressIds: Collection<Long>) {
        if (addressIds.isEmpty()) return
        viewModelScope.launch {
            routes.deleteAddresses(addressIds)
            stopSelecting()
        }
    }

    fun clearAddError() {
        _addError.value = null
    }

    /** Adds a number with optional addition (e.g. 14 + "A"); reports invalid input and duplicates. */
    fun addNumber(numberText: String, additionText: String) {
        val number = numberText.trim().toIntOrNull()
        if (number == null || number <= 0) {
            _addError.value = AddNumberError.InvalidNumber
            return
        }
        viewModelScope.launch {
            try {
                routes.addAddress(segmentId, number, additionText)
                _addError.value = null
                _added.value += 1
            } catch (e: DuplicateAddressException) {
                _addError.value = AddNumberError.Duplicate("${e.houseNumber}${e.addition.orEmpty()}")
            }
        }
    }

    fun clearBuildingError() {
        _buildingError.value = null
    }

    /** Turns [address]'s number into a building with units [from]..[to] (BLD-01). */
    fun createBuilding(address: Address, type: SuffixType, from: String, to: String) {
        if (unitsOrNull(from, to, type) == null) {
            _buildingError.value = BuildingError.InvalidRange
            return
        }
        viewModelScope.launch {
            try {
                routes.createBuilding(segmentId, address.houseNumber, type, from, to)
                _buildingError.value = null
                _buildingCreated.value += 1
            } catch (e: BuildingConflictException) {
                _buildingError.value = BuildingError.Conflict(e.labels)
            }
        }
    }

    /** "No longer a building": removes it and its apartments and restores the plain number. */
    fun removeBuilding(buildingId: Long) {
        viewModelScope.launch { routes.removeBuilding(buildingId) }
    }

    fun deleteSection() {
        viewModelScope.launch { routes.deleteSegment(segmentId) }
    }
}

/**
 * Merges standalone houses and buildings into walking order. A building sorts like its plain
 * number, so "12 · 12 apartments" appears where number 12 was, and also when it has no apartments.
 */
internal fun cellsInWalkingOrder(
    segment: Segment,
    standalone: List<Address>,
    buildings: List<Building>,
    allAddresses: List<Address>,
): List<SegmentCell> {
    val apartmentsByBuilding = allAddresses.filter { it.buildingId != null }.groupBy { it.buildingId }
    val keyed: List<Pair<Address, SegmentCell>> =
        standalone.map { it to SegmentCell.House(it) } +
            buildings.map { building ->
                val apartments = apartmentsByBuilding[building.id].orEmpty()
                val summary = BuildingSummary(
                    building = building,
                    apartments = apartments,
                    stickers = stickerSummary(apartments),
                    counts = deliverySummary(apartments, FullRound),
                )
                // Sorts as the plain number (no addition); ties with standalone 12A etc. put the building first.
                Address(houseNumber = building.houseNumber) to SegmentCell.Apartments(summary)
            }
    val ascending = keyed.sortedWith(compareBy(AddressNumberOrder) { it.first })
    return (if (segment.direction == Direction.DESCENDING) ascending.reversed() else ascending).map { it.second }
}
