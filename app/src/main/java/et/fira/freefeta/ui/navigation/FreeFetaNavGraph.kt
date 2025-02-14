package et.fira.freefeta.ui.navigation

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import et.fira.freefeta.ui.AppViewModelProvider
import et.fira.freefeta.ui.about.AboutDestination
import et.fira.freefeta.ui.about.AboutScreen
import et.fira.freefeta.ui.ad.AdDialog
import et.fira.freefeta.ui.ad.AdViewModel
import et.fira.freefeta.ui.home.HomeDestination
import et.fira.freefeta.ui.home.HomeScreen
import et.fira.freefeta.ui.player.PlayerDestination
import et.fira.freefeta.ui.player.PlayerScreen
import et.fira.freefeta.ui.settings.SettingsDestination
import et.fira.freefeta.ui.settings.SettingsScreen

@OptIn(UnstableApi::class)
@Composable
fun FreeFetaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    windowSize: WindowWidthSizeClass
) {
    val adViewModel: AdViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val currentAd by adViewModel.adState

    // Show Ad if it exist

    Box {
        NavHost(
            navController = navController,
            startDestination = HomeDestination.route,
            modifier = modifier
        ) {
            composable(route = HomeDestination.route) {
                HomeScreen(
                    navigateTo = navController::navigate,
                    adViewModel = adViewModel,
                    windowSize = windowSize
                )
            }
            composable(route = "${PlayerDestination.route}/{${PlayerDestination.arg}}") { backStackEntry ->
                val filePath =
                    Uri.decode(backStackEntry.arguments?.getString(PlayerDestination.arg) ?: "")
                PlayerScreen(
                    filePath = filePath,
                ) {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(HomeDestination.route) {
                            inclusive = true
                        }
                    }
                }
            }
            composable(route = SettingsDestination.route) {
                SettingsScreen(
                    navController = navController
                )
            }
            composable(route = AboutDestination.route) {
                AboutScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
        }

        currentAd?.let { ad ->
            AdDialog(
                ad = ad,
                onDismiss = { adViewModel.onAdDismissed() }
            )
        }
    }
}