package et.fira.freefeta.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import et.fira.freefeta.data.ThemeMode
import et.fira.freefeta.ui.about.AboutDestination
import et.fira.freefeta.ui.home.HomeDestination
import et.fira.freefeta.ui.navigation.FreeFetaNavHost
import et.fira.freefeta.ui.navigation.NavigationDestination
import et.fira.freefeta.ui.settings.SettingsDestination
import et.fira.freefeta.ui.theme.FreeFetaTheme

enum class AppDestinations(
    val destination: NavigationDestination,
    val icon: ImageVector
) {
    HOME(HomeDestination, Icons.Default.Home),
    SETTINGS(SettingsDestination, Icons.Default.Settings),
    About(AboutDestination, Icons.Default.Info)
}

@Composable
fun FreeFetaApp(
    themeMode: ThemeMode
) {
//    val themeMode = freeFetaViewModel.themeMode.collectAsState()
    val isDarkTheme: Boolean = when(themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    FreeFetaTheme(
        darkTheme = isDarkTheme
    ) {

        FreeFetaNavHost()
    }
}

