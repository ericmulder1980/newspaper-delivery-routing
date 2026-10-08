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
import nl.ericmulder.krantenwijk.domain.model.unitLabel
import nl.ericmulder.krantenwijk.domain.model.unitPrefix
import nl.ericmulder.krantenwijk.domain.repository.DuplicateAddressException
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.rules.DeliveryCounts
import nl.ericmulder.krantenwijk.domain.rules.FullRound
import nl.ericmulder.krantenwijk.domain.rules.deliverySummary
import nl.ericmulder.krantenwijk.domain.rules.normaliseSuffix

sealed interface BuildingDetailUiState {
    data object Loading : BuildingDetailUiState

    /** The building no longer exists (removed from the street screen). */
    data object Gone : BuildingDetailUiState

    data class Ready(
        val building: Building,
        val streetName: String,
        /** Apartments sorted like the bank of mailboxes: A, B … Z or 1, 2 … 20. */
        val apartments: List<Address>,
        val existingCount: Int,
        val counts: DeliveryCounts,
        /** Non-null while selecting apartments for a bulk change (BLD-04). */
        val selection: Set<Long>?,
    ) : BuildingDetailUiState {
        val selecting: Boolean get() = selection != null
    }
}

/** Why an apartment couldn't be added. */
sealed interface AddApartmentError {
    data object Invalid : AddApartmentError

    data class Duplicate(val label: String) : AddApartmentError
}

/** One apartment building, zoomed in: stickers per apartment and in bulk, add and delete apartments (BLD-03..05). */
@HiltViewModel(assistedFactory = BuildingDetailViewModel.Factory::class)
class BuildingDetailViewModel @AssistedInject constructor(
    @Assisted private val buildingId: Long,
    private val routes: RouteRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(buildingId: Long): BuildingDetailViewModel
    }

    private val selection = MutableStateFlow<Set<Long>?>(null)

    val uiState: StateFlow<BuildingDetailUiState> =
        combine(routes.observeBuildingContents(buildingId), selection) { contents, selected ->
            if (contents == null) {
                BuildingDetailUiState.Gone
            } else {
                BuildingDetailUiState.Ready(
                    building = contents.building,
                    streetName = contents.streetName,
                    apartments = contents.apartments,
                    existingCount = contents.apartments.count { it.exists },
                    counts = deliverySummary(contents.apartments, FullRound),
                    selection = selected?.intersect(contents.apartments.mapTo(HashSet()) { it.id }),
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BuildingDetailUiState.Loading)

    private val _addError = MutableStateFlow<AddApartmentError?>(null)
    val addError: StateFlow<AddApartmentError?> = _addError.asStateFlow()

    private val _added = MutableStateFlow(0)

    /** Increments after each added apartment, so the screen can close the add sheet. */
    val added: StateFlow<Int> = _added.asStateFlow()

    fun startSelecting() {
        selection.value = emptySet()
    }

    fun stopSelecting() {
        selection.value = null
    }

    fun toggleSelected(apartment: Address) = selection.update { current ->
        current?.let { if (apartment.id in it) it - apartment.id else it + apartment.id }
    }

    fun selectAll() {
        val ready = uiState.value as? BuildingDetailUiState.Ready ?: return
        selection.value = ready.apartments.mapTo(HashSet()) { it.id }
    }

    /** Same rules as on the street screen: a sticker also restores "does not exist" apartments. */
    fun apply(option: StickerOption, apartmentIds: Collection<Long>) {
        if (apartmentIds.isEmpty()) return
        viewModelScope.launch {
            routes.applyStickerOption(option, apartmentIds)
            stopSelecting()
        }
    }

    fun delete(apartmentIds: Collection<Long>) {
        if (apartmentIds.isEmpty()) return
        viewModelScope.launch {
            routes.deleteAddresses(apartmentIds)
            stopSelecting()
        }
    }

    fun clearAddError() {
        _addError.value = null
    }

    /** Adds apartment [input] (a letter or number, depending on the building) (BLD-05). */
    fun addApartment(input: String) {
        val ready = uiState.value as? BuildingDetailUiState.Ready ?: return
        if (normaliseSuffix(input, ready.building.suffixType) == null) {
            _addError.value = AddApartmentError.Invalid
            return
        }
        viewModelScope.launch {
            try {
                routes.addApartment(buildingId, input)
                _addError.value = null
                _added.value += 1
            } catch (e: DuplicateAddressException) {
                _addError.value = AddApartmentError.Duplicate(ready.building.unitLabel(e.addition.orEmpty().removePrefix(ready.building.unitPrefix)))
            }
        }
    }
}
