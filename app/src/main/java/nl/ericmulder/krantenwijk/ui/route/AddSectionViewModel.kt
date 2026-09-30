package nl.ericmulder.krantenwijk.ui.route

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.rules.SectionPreview
import nl.ericmulder.krantenwijk.domain.rules.previewSection
import nl.ericmulder.krantenwijk.domain.rules.suggestStreetNames
import javax.inject.Inject

/** Longest house number accepted in the From/To fields. */
private const val MAX_NUMBER_DIGITS = 4

data class AddSectionForm(
    val street: String = "",
    val side: Side = Side.EVEN,
    val from: String = "",
    val to: String = "",
    val reverse: Boolean = false,
)

data class AddSectionUiState(
    val form: AddSectionForm,
    /** 1-based number of the section being added ("Street section 3"). */
    val sectionNumber: Int,
    val suggestions: List<String>,
    val preview: SectionPreview,
    val saving: Boolean,
    val saveFailed: Boolean,
) {
    val canSave: Boolean get() = form.street.isNotBlank() && preview.canSave && !saving
}

/** Adding a street section (ADR-02, ADR-07, ADR-11). The form survives process death. */
@HiltViewModel
class AddSectionViewModel @Inject constructor(
    private val routes: RouteRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val form = MutableStateFlow(
        AddSectionForm(
            street = savedState[KEY_STREET] ?: "",
            side = savedState.get<String>(KEY_SIDE)?.let(Side::valueOf) ?: Side.EVEN,
            from = savedState[KEY_FROM] ?: "",
            to = savedState[KEY_TO] ?: "",
            reverse = savedState[KEY_REVERSE] ?: false,
        ),
    )
    private val status = MutableStateFlow(SaveStatus())

    private val _saved = MutableStateFlow(false)

    /** True once the section is stored; the screen then navigates back. */
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    val uiState: StateFlow<AddSectionUiState> = combine(
        form,
        status,
        routes.observeStreetNames(),
        routes.observeSegments(),
    ) { form, status, streetNames, segments ->
        AddSectionUiState(
            form = form,
            sectionNumber = segments.size + 1,
            suggestions = suggestStreetNames(streetNames, form.street),
            preview = previewSection(
                from = form.from.toIntOrNull(),
                to = form.to.toIntOrNull(),
                side = form.side,
                direction = if (form.reverse) Direction.DESCENDING else Direction.ASCENDING,
            ),
            saving = status.saving,
            saveFailed = status.failed,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState())

    fun setStreet(street: String) = edit { it.copy(street = street) }

    fun setSide(side: Side) = edit { it.copy(side = side) }

    fun setFrom(value: String) = edit { it.copy(from = digitsOnly(value)) }

    fun setTo(value: String) = edit { it.copy(to = digitsOnly(value)) }

    fun setReverse(reverse: Boolean) = edit { it.copy(reverse = reverse) }

    fun save() {
        val state = uiState.value
        if (!state.canSave) return
        val form = state.form
        status.value = SaveStatus(saving = true)
        viewModelScope.launch {
            try {
                routes.addSegment(
                    streetName = form.street,
                    side = form.side,
                    from = checkNotNull(form.from.toIntOrNull()),
                    to = checkNotNull(form.to.toIntOrNull()),
                    direction = if (form.reverse) Direction.DESCENDING else Direction.ASCENDING,
                )
                _saved.value = true
            } catch (e: Exception) {
                status.value = SaveStatus(failed = true)
            }
        }
    }

    private fun edit(change: (AddSectionForm) -> AddSectionForm) {
        form.update { current ->
            change(current).also {
                savedState[KEY_STREET] = it.street
                savedState[KEY_SIDE] = it.side.name
                savedState[KEY_FROM] = it.from
                savedState[KEY_TO] = it.to
                savedState[KEY_REVERSE] = it.reverse
            }
        }
        status.update { it.copy(failed = false) }
    }

    private fun digitsOnly(value: String) = value.filter(Char::isDigit).take(MAX_NUMBER_DIGITS)

    private fun initialState() = AddSectionUiState(
        form = form.value,
        sectionNumber = 1,
        suggestions = emptyList(),
        preview = previewSection(null, null, form.value.side, Direction.ASCENDING),
        saving = false,
        saveFailed = false,
    )

    private data class SaveStatus(val saving: Boolean = false, val failed: Boolean = false)

    private companion object {
        const val KEY_STREET = "street"
        const val KEY_SIDE = "side"
        const val KEY_FROM = "from"
        const val KEY_TO = "to"
        const val KEY_REVERSE = "reverse"
    }
}
