package et.fira.freefeta.ui.navigation

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
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
import et.fira.freefeta.ui.home.HomeViewModel
import et.fira.freefeta.ui.onboarding.OnboardingScreen
import et.fira.freefeta.ui.onboarding.OnboardingScreenViewModel
import et.fira.freefeta.ui.player.PlayerDestination
import et.fira.freefeta.ui.player.PlayerScreen
import et.fira.freefeta.ui.settings.SettingsDestination
import et.fira.freefeta.ui.settings.SettingsScreen
import et.fira.freefeta.ui.update.AppUpdateDialog

@OptIn(UnstableApi::class)
@Composable
fun FreeFetaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current

    val adViewModel: AdViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val currentAd by adViewModel.adState
    val howeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)

    LaunchedEffect(Unit) {
        howeViewModel.fetchNewFilesAndNotify(context)
    }

    // Show Ad if it exist

    Box {
        NavHost(
            navController = navController,
            startDestination = HomeDestination.route,
            modifier = modifier
        ) {
            composable(route = HomeDestination.route) {
                HomeScreen(
                    viewModel = howeViewModel,
                    navigateTo = navController::navigate,
                    adViewModel = adViewModel,
                )
            }
            composable(route = "${PlayerDestination.route}/{${PlayerDestination.ARG}}") { backStackEntry ->
                val filePath =
                    Uri.decode(backStackEntry.arguments?.getString(PlayerDestination.ARG) ?: "")
                PlayerScreen(
                    filePath = filePath,
                    onBackPressed = {
                        navController.navigate(HomeDestination.route) {
                            popUpTo(HomeDestination.route) {
                                inclusive = true
                            }
                        }
                    }
                )
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
        val onBoardingViewModel: OnboardingScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val onboardingCompleted by onBoardingViewModel.onboardingCompleted.collectAsState(initial = true)

        currentAd?.let { ad ->
            if (onboardingCompleted) {
                AdDialog(
                    ad = ad,
                    onDismiss = { adViewModel.onAdDismissed() }
                )
            }
        }

        AppUpdateDialog()
        if (!onboardingCompleted) {
            OnboardingScreen {
                onBoardingViewModel.completeOnboarding()
            }
        }
    }
}