package nl.ericmulder.krantenwijk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.ui.home.HomeScreen
import nl.ericmulder.krantenwijk.ui.placeholder.PlaceholderLink
import nl.ericmulder.krantenwijk.ui.placeholder.PlaceholderScreen
import nl.ericmulder.krantenwijk.ui.round.RoundOverviewScreen
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
 */
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(SavedStateConfig, Home)
    fun go(destination: Destination) {
        backStack.add(destination)
    }
    fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
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
                    onStartRound = { go(RoundOverview) },
                    onEditRoute = { go(RouteEditor) },
                    onSettings = { go(Settings) },
                )
            }
            entry<Settings> { SettingsScreen(onBack = ::back) }
            entry<Onboarding> {
                PlaceholderScreen(stringResource(R.string.onboarding_title), "ONB-A", ::back)
            }
            entry<RouteEditor> {
                RouteEditorScreen(
                    onBack = ::back,
                    onAddSection = { go(AddSection) },
                    onOpenSection = { go(SegmentDetail(it)) },
                )
            }
            entry<AddSection> {
                AddSectionScreen(
                    onBack = ::back,
                    // Replace the form with the number check, so back from there returns to the route.
                    onSaved = { id ->
                        backStack.removeAt(backStack.lastIndex)
                        go(SegmentDetail(id))
                    },
                )
            }
            entry<SegmentDetail> { key ->
                SegmentDetailScreen(segmentId = key.segmentId, onBack = ::back, onOpenBuilding = { go(BuildingDetail(it)) })
            }
            entry<BuildingDetail> { key -> BuildingDetailScreen(buildingId = key.buildingId, onBack = ::back) }
            entry<AddressDetail> { PlaceholderScreen(stringResource(R.string.address_title), "STK-A", ::back) }
            entry<RoundOverview> {
                RoundOverviewScreen(
                    onBack = ::back,
                    onOpenSection = { go(RoundStreet(it)) },
                    onEditRoute = { go(RouteEditor) },
                )
            }
            entry<RoundStreet> { key ->
                RoundStreetScreen(
                    segmentId = key.segmentId,
                    onBack = ::back,
                    // Previous/Next replace the street, so back always returns to the overview.
                    onGoTo = { id ->
                        backStack.removeAt(backStack.lastIndex)
                        go(RoundStreet(id))
                    },
                    onFinish = { while (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                    onOpenBuilding = { go(RoundBuilding(it)) },
                )
            }
            entry<RoundBuilding> { key -> BuildingDetailScreen(buildingId = key.buildingId, onBack = ::back, roundMode = true) }
        },
    )
}
