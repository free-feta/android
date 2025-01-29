package et.fira.freefeta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import et.fira.freefeta.FreeFetaApplication
import et.fira.freefeta.data.FileDownloaderRepository

class FreeFetaViewModel(
    private val videoFileDownloaderRepository: FileDownloaderRepository
): ViewModel() {

    init {
        videoFileDownloaderRepository.download("https://telebirrchat.ethiomobilemoney.et:21006/sfs/ufile?digest=fid5e4d5ff5bb0f20b7e70ec3d0bb01d1d2&filename=Khalid+-+Young+Dumb+_+Broke+(Lyrics)(720P_HD).mp4")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FreeFetaApplication)
                val videoFileDownloaderRepository = application.container.fileDownloaderRepository
                FreeFetaViewModel(videoFileDownloaderRepository = videoFileDownloaderRepository)
            }
        }
    }
}