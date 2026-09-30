package nl.ericmulder.krantenwijk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import nl.ericmulder.krantenwijk.ui.home.HomeScreen
import nl.ericmulder.krantenwijk.ui.onboarding.OnboardingNameScreen
import nl.ericmulder.krantenwijk.ui.onboarding.OnboardingRouteScreen
import nl.ericmulder.krantenwijk.ui.onboarding.OnboardingSectionsScreen
import nl.ericmulder.krantenwijk.ui.round.RoundStreetScreen
import nl.ericmulder.krantenwijk.ui.route.AddSectionScreen
import nl.ericmulder.krantenwijk.ui.route.BuildingDetailScreen
import nl.ericmulder.krantenwijk.ui.route.RouteEditorScreen
import nl.ericmulder.krantenwijk.ui.route.SegmentDetailScreen
import nl.ericmulder.krantenwijk.ui.settings.SettingsScreen

private val SavedStateConfig = SavedStateConfiguration { serializersModule = DestinationSerializersModule }

/**
 * App navigation (Navigation 3). The back stack is a plain list of [Destination] keys: navigating
 * adds a key, back removes the last one. Each entry gets its own saved state and ViewModels.
 *
 * @param startWithSetup open the setup wizard instead of Home (first launch, ONB-A).
 */
@Composable
fun AppNavigation(startWithSetup: Boolean) {
    val backStack = rememberNavBackStack(SavedStateConfig, if (startWithSetup) OnboardingName else Home)
    fun go(destination: Destination) {
        backStack.add(destination)
    }
    fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    fun replaceTop(destination: Destination) {
        backStack.removeAt(backStack.lastIndex)
        go(destination)
    }
    fun resetTo(destination: Destination) {
        backStack.clear()
        go(destination)
    }
    /** Wizard: after checking a street's numbers, return to (or open) the walking route step. */
    fun toWizardSections() {
        val index = backStack.indexOfLast { it == OnboardingSections }
        if (index >= 0) {
            while (backStack.lastIndex > index) backStack.removeAt(backStack.lastIndex)
        } else {
            replaceTop(OnboardingSections)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = ::back,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    onStartAt = { go(RoundStreet(it)) },
                    onEditRoute = { go(RouteEditor) },
                    onSettings = { go(Settings) },
                )
            }
            entry<Settings> { SettingsScreen(onBack = ::back) }

            // Setup wizard (ONB-A): name → route → street + numbers (repeat) → walking route.
            entry<OnboardingName> { OnboardingNameScreen(onNext = { go(OnboardingRoute) }) }
            entry<OnboardingRoute> {
                OnboardingRouteScreen(
                    onBack = ::back,
                    onNext = { if (backStack.contains(OnboardingSections)) toWizardSections() else go(AddSection(wizard = true)) },
                )
            }
            entry<OnboardingSections> {
                OnboardingSectionsScreen(
                    onBack = ::back,
                    onAddSection = { go(AddSection(wizard = true)) },
                    onOpenSection = { go(SegmentDetail(it, wizard = true)) },
                    onFinished = { resetTo(Home) },
                )
            }

            entry<RouteEditor> {
                RouteEditorScreen(
                    onBack = ::back,
                    onAddSection = { go(AddSection()) },
                    onOpenSection = { go(SegmentDetail(it)) },
                )
            }
            entry<AddSection> { key ->
                AddSectionScreen(
                    onBack = ::back,
                    wizardStep = if (key.wizard) 3 else null,
                    // Replace the form with the number check, so back from there returns to where we came from.
                    onSaved = { id -> replaceTop(SegmentDetail(id, wizard = key.wizard)) },
                )
            }
            entry<SegmentDetail> { key ->
                SegmentDetailScreen(
                    segmentId = key.segmentId,
                    onBack = ::back,
                    onDone = if (key.wizard) ::toWizardSections else ::back,
                    wizardStep = if (key.wizard) 3 else null,
                    onOpenBuilding = { go(BuildingDetail(it)) },
                )
            }
            entry<BuildingDetail> { key -> BuildingDetailScreen(buildingId = key.buildingId, onBack = ::back) }
            entry<RoundStreet> { key ->
                RoundStreetScreen(
                    segmentId = key.segmentId,
                    onBack = ::back,
                    // Previous/Next replace the street, so back always returns home.
                    onGoTo = { replaceTop(RoundStreet(it)) },
                    onFinish = { resetTo(Home) },
                    onOpenBuilding = { go(RoundBuilding(it)) },
                )
            }
            entry<RoundBuilding> { key -> BuildingDetailScreen(buildingId = key.buildingId, onBack = ::back, roundMode = true) }
        },
    )
}

private fun List<NavKey>.contains(key: Destination) = any { it == key }
