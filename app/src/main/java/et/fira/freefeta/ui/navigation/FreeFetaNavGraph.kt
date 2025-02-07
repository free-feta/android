package et.fira.freefeta.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import et.fira.freefeta.ui.about.AboutDestination
import et.fira.freefeta.ui.about.AboutScreen
import et.fira.freefeta.ui.home.HomeDestination
import et.fira.freefeta.ui.home.HomeScreen
import et.fira.freefeta.ui.player.PlayerDestination
import et.fira.freefeta.ui.player.PlayerScreen
import et.fira.freefeta.ui.settings.SettingsDestination
import et.fira.freefeta.ui.settings.SettingsScreen

@Composable
fun FreeFetaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSize: WindowWidthSizeClass
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateTo = navController::navigate,
                windowSize = windowSize
            )
        }
        composable(route = PlayerDestination.route) {
            PlayerScreen(
                navController = navController
            )
        }
        composable(route = SettingsDestination.route) {
            SettingsScreen(
                navController = navController
            )
        }
        composable(route = AboutDestination.route) {
            AboutScreen(
                navigateBack = {navController.popBackStack()}
            )
        }
    }
}