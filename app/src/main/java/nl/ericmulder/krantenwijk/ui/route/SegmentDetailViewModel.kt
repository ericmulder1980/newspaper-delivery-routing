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
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.repository.DuplicateAddressException
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.rules.DeliveryCounts
import nl.ericmulder.krantenwijk.domain.rules.FullRound
import nl.ericmulder.krantenwijk.domain.rules.deliverySummary

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

sealed interface SegmentDetailUiState {
    data object Loading : SegmentDetailUiState

    /** The segment no longer exists (deleted here or elsewhere). */
    data object Gone : SegmentDetailUiState

    data class Ready(
        val segment: Segment,
        /** 1-based place in the walking order. */
        val position: Int,
        /** Standalone addresses in walking order (buildings arrive with BLD-A). */
        val addresses: List<Address>,
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
                existingCount = contents.addresses.count { it.exists },
                counts = deliverySummary(contents.addresses, FullRound),
                // Numbers deleted meanwhile drop out of the selection.
                selection = selected?.intersect(ids),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SegmentDetailUiState.Loading)

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

    fun deleteSection() {
        viewModelScope.launch { routes.deleteSegment(segmentId) }
    }
}
