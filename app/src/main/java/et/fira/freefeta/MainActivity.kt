package et.fira.freefeta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import et.fira.freefeta.ui.FreeFetaApp

class MainActivity : ComponentActivity() {
    private val viewModel: SplashViewModel by viewModels { SplashViewModelFactory(application) }
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        splashscreen.setKeepOnScreenCondition { viewModel.uiState.value.isLoading }

        enableEdgeToEdge()
        setContent {
            val themeMode = viewModel.uiState.collectAsStateWithLifecycle()
            FreeFetaApp(
                themeMode = themeMode.value.theme,
            )
        }
    }
}
