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
import nl.ericmulder.krantenwijk.ui.route.AddSectionScreen
import nl.ericmulder.krantenwijk.ui.route.RouteEditorScreen
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
                    onStartRound = { go(RoundStart) },
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
            entry<AddSection> { AddSectionScreen(onBack = ::back, onSaved = ::back) }
            entry<SegmentDetail> {
                PlaceholderScreen(
                    title = stringResource(R.string.segment_title),
                    featureId = "ADR-B",
                    onBack = ::back,
                    links = listOf(
                        PlaceholderLink(stringResource(R.string.address_title)) { go(AddressDetail(addressId = 0)) },
                        PlaceholderLink(stringResource(R.string.building_title)) { go(BuildingDetail(buildingId = 0)) },
                    ),
                )
            }
            entry<BuildingDetail> { PlaceholderScreen(stringResource(R.string.building_title), "BLD-B", ::back) }
            entry<AddressDetail> { PlaceholderScreen(stringResource(R.string.address_title), "ADR-B", ::back) }
            entry<RoundStart> {
                PlaceholderScreen(
                    title = stringResource(R.string.round_start_title),
                    featureId = "RND-A",
                    onBack = ::back,
                    links = listOf(
                        PlaceholderLink(stringResource(R.string.round_title)) { go(RoundMode(newspaper = true, leaflets = false)) },
                    ),
                )
            }
            entry<RoundMode> { PlaceholderScreen(stringResource(R.string.round_title), "RND-B", ::back) }
        },
    )
}
