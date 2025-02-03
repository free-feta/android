package et.fira.freefeta.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import et.fira.freefeta.ui.navigation.FreeFetaNavHost
import et.fira.freefeta.ui.theme.FreeFetaTheme

@Composable
fun FreeFetaApp(navController: NavHostController = rememberNavController()) {
//    val themeMode = freeFetaViewModel.themeMode.collectAsState()
//    val isDarkTheme: Boolean = when(themeMode.value) {
//        ThemeMode.LIGHT -> false
//        ThemeMode.DARK -> true
//        ThemeMode.SYSTEM -> isSystemInDarkTheme()
//    }
    FreeFetaTheme() {
        FreeFetaNavHost(navController = navController)
    }
}
