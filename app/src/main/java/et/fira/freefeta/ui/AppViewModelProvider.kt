package et.fira.freefeta.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import et.fira.freefeta.FreeFetaApplication
import et.fira.freefeta.ui.home.HomeViewModel
import et.fira.freefeta.ui.player.PlayerViewModel
import et.fira.freefeta.ui.settings.SettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            SettingsViewModel(
                userPreferencesRepository = freeFetaApplication().container.userPreferencesRepository
            )
        }

        initializer {
            PlayerViewModel(
                localFileRepository = freeFetaApplication().container.localFileRepository
            )
        }

        initializer {
            HomeViewModel(
                fileDownloaderRepository = freeFetaApplication().container.fileDownloaderRepository,
                localFileRepository = freeFetaApplication().container.localFileRepository,
                remoteFileRepository = freeFetaApplication().container.remoteFileRepository,
                userPreferencesRepository = freeFetaApplication().container.userPreferencesRepository
            )
        }
    }
}

fun CreationExtras.freeFetaApplication(): FreeFetaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FreeFetaApplication)