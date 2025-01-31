package et.fira.freefeta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import et.fira.freefeta.FreeFetaApplication
import et.fira.freefeta.data.FileDownloaderRepository
import et.fira.freefeta.data.MediaRepository
import et.fira.freefeta.model.Media
import kotlinx.coroutines.launch

class FreeFetaViewModel(
    private val videoFileDownloaderRepository: FileDownloaderRepository,
    private val mediaRepository: MediaRepository
): ViewModel() {

    init {
//        videoFileDownloaderRepository.download("https://telebirrchat.ethiomobilemoney.et:21006/sfs/ufile?digest=fid5e4d5ff5bb0f20b7e70ec3d0bb01d1d2&filename=Khalid+-+Young+Dumb+_+Broke+(Lyrics)(720P_HD).mp4")
        viewModelScope.launch {
            mediaRepository.insertMedia(
                Media(
                    id = 1,
                    title = "my media",
                    downloadUrl = "https://google.com",
                    createdAt = 10000000
                )
            )
        }

    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FreeFetaApplication)
                val videoFileDownloaderRepository = application.container.fileDownloaderRepository
                val mediaRepository = application.container.mediaRepository
                FreeFetaViewModel(
                    videoFileDownloaderRepository = videoFileDownloaderRepository,
                    mediaRepository = mediaRepository
                )
            }
        }
    }
}