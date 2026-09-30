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
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.repository.DuplicateAddressException
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository

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
    ) : SegmentDetailUiState
}

/** Result of trying to add a number, shown in the add sheet. */
sealed interface AddNumberError {
    data object InvalidNumber : AddNumberError

    data class Duplicate(val label: String) : AddNumberError
}

/** Checking and editing the numbers of one street section (ADR-12, ADR-03, ADR-04). */
@HiltViewModel(assistedFactory = SegmentDetailViewModel.Factory::class)
class SegmentDetailViewModel @AssistedInject constructor(
    @Assisted private val segmentId: Long,
    private val routes: RouteRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(segmentId: Long): SegmentDetailViewModel
    }

    val uiState: StateFlow<SegmentDetailUiState> = combine(
        routes.observeSegmentContents(segmentId),
        routes.observeSegments(),
    ) { contents, segments ->
        if (contents == null) {
            SegmentDetailUiState.Gone
        } else {
            val standalone = contents.addresses.filter { it.buildingId == null }
            SegmentDetailUiState.Ready(
                segment = contents.segment,
                position = segments.indexOfFirst { it.id == segmentId } + 1,
                addresses = standalone,
                existingCount = contents.addresses.count { it.exists },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SegmentDetailUiState.Loading)

    private val _addError = MutableStateFlow<AddNumberError?>(null)
    val addError: StateFlow<AddNumberError?> = _addError.asStateFlow()

    private val _added = MutableStateFlow(0)

    /** Increments after each successful add, so the screen can close the add sheet. */
    val added: StateFlow<Int> = _added.asStateFlow()

    fun setExists(address: Address, exists: Boolean) {
        viewModelScope.launch { routes.setExists(listOf(address.id), exists) }
    }

    fun delete(address: Address) {
        viewModelScope.launch { routes.deleteAddresses(listOf(address.id)) }
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
