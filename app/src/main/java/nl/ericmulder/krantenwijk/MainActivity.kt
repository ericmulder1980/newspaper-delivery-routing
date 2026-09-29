package nl.ericmulder.krantenwijk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var routeRepository: RouteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Skeleton only: theme and navigation arrive in FND-002. Reading the route here opens the
        // database on the device, so a broken Room/SQLite/R8 setup shows up before real screens exist.
        setContent {
            val routeFlow = remember { routeRepository.observeRoute() }
            val route by routeFlow.collectAsStateWithLifecycle(initialValue = null)
            MaterialTheme {
                Scaffold { padding ->
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text(
                            text = route?.name ?: stringResource(R.string.skeleton_greeting),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }
        }
    }
}
