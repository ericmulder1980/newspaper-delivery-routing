package nl.ericmulder.krantenwijk

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import nl.ericmulder.krantenwijk.domain.model.AppSettings
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository
import nl.ericmulder.krantenwijk.ui.navigation.AppNavigation
import nl.ericmulder.krantenwijk.ui.theme.KrantenwijkTheme
import nl.ericmulder.krantenwijk.ui.theme.isDark
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsFlow = remember { settingsRepository.settings }
            val settings by settingsFlow.collectAsStateWithLifecycle(initialValue = null)
            val dark = (settings ?: AppSettings()).theme.isDark()

            // Status and navigation bar icons follow the app theme, not only the system theme.
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            KrantenwijkTheme(dark = dark) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    // Wait for the stored theme so the screen doesn't flash in the wrong theme.
                    if (settings != null) AppNavigation()
                }
            }
        }
    }
}
