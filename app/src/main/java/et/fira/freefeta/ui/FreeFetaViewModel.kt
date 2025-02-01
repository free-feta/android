package et.fira.freefeta.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import et.fira.freefeta.FreeFetaApplication
import et.fira.freefeta.data.file.FileDownloaderRepository
import et.fira.freefeta.data.file.LocalFileRepository
import et.fira.freefeta.data.file.RemoteFileRepository
import et.fira.freefeta.data.ThemeMode
import et.fira.freefeta.data.UserPreferencesRepository
import et.fira.freefeta.model.FileEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class FreeFetaViewModel(
    private val fileDownloaderRepository: FileDownloaderRepository,
    private val localFileRepository: LocalFileRepository,
    private val remoteFileRepository: RemoteFileRepository,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )


    init {
//        videoFileDownloaderRepository.download("https://telebirrchat.ethiomobilemoney.et:21006/sfs/ufile?digest=fid5e4d5ff5bb0f20b7e70ec3d0bb01d1d2&filename=Khalid+-+Young+Dumb+_+Broke+(Lyrics)(720P_HD).mp4")
        viewModelScope.launch {
            localFileRepository.insertFile(
                FileEntity(
                    id = 1,
                    name = "my media",
                    downloadUrl = "https://google.com",
                    createdAt = 10000000
                )
            )
        }

    }

    fun setTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(themeMode)
        }
    }

    fun getMarsPhotos() {
        viewModelScope.launch {
            try {
                val listResult = remoteFileRepository.getFiles()
                Log.d("FreeFetaViewModel", "Success: ${listResult.toString()} Mars photos retrieved")

            } catch (e: IOException) {
                Log.e("", e.message ?: "")
            }
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FreeFetaApplication)
                val container = application.container
                FreeFetaViewModel(
                    fileDownloaderRepository = container.fileDownloaderRepository,
                    localFileRepository = container.localFileRepository,
                    remoteFileRepository = container.remoteFileRepository,
                    userPreferencesRepository = container.userPreferencesRepository
                )
            }
        }
    }
}