package nl.ericmulder.krantenwijk.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.ericmulder.krantenwijk.R
import nl.ericmulder.krantenwijk.ui.common.AccentButton
import nl.ericmulder.krantenwijk.ui.common.ScreenScaffold
import nl.ericmulder.krantenwijk.ui.common.SecondaryButton

/** Home (plan §6). The sticker overview (STK-04) and route summary arrive with later features. */
@Composable
fun HomeScreen(
    onStartRound: () -> Unit,
    onEditRoute: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val routeName by viewModel.routeName.collectAsStateWithLifecycle()
    ScreenScaffold(title = stringResource(R.string.app_name), onBack = null) {
        Text(
            text = routeName ?: stringResource(R.string.home_no_route),
            style = MaterialTheme.typography.titleLarge,
        )
        AccentButton(text = stringResource(R.string.home_start_round), onClick = onStartRound)
        SecondaryButton(text = stringResource(R.string.home_edit_route), onClick = onEditRoute)
        SecondaryButton(text = stringResource(R.string.settings_title), onClick = onSettings)
    }
}
