package et.fira.freefeta.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import et.fira.freefeta.FreeFetaApplication
import et.fira.freefeta.ui.ad.AdViewModel
import et.fira.freefeta.ui.home.HomeViewModel
import et.fira.freefeta.ui.network.NetworkStatusViewModel
import et.fira.freefeta.ui.onboarding.OnboardingScreenViewModel
import et.fira.freefeta.ui.player.PlayerViewModel
import et.fira.freefeta.ui.settings.SettingsViewModel
import et.fira.freefeta.ui.update.UpdateViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            OnboardingScreenViewModel(
                userPreferencesRepository = freeFetaApplication().container.userPreferencesRepository
            )
        }

        initializer {
            NetworkStatusViewModel(
                application = freeFetaApplication(),
            )
        }

        initializer {
            UpdateViewModel(
                configRepository = freeFetaApplication().container.appConfigRepository,
                fileDownloaderRepository = freeFetaApplication().container.fileDownloaderRepository
            )
        }

        initializer {
            AdViewModel(
                adRepository = freeFetaApplication().container.adRepository
            )
        }

        initializer {
            SettingsViewModel(
                userPreferencesRepository = freeFetaApplication().container.userPreferencesRepository,
            )
        }

        initializer {
            PlayerViewModel(
                userPreferencesRepository = freeFetaApplication().container.userPreferencesRepository,
            )
        }

        initializer {
            HomeViewModel(
                fileDownloaderRepository = freeFetaApplication().container.fileDownloaderRepository,
                localFileRepository = freeFetaApplication().container.localFileRepository,
                remoteFileRepository = freeFetaApplication().container.remoteFileRepository,
                userPreferencesRepository = freeFetaApplication().container.userPreferencesRepository,
            )
        }
    }
}

fun CreationExtras.freeFetaApplication(): FreeFetaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FreeFetaApplication)