package et.fira.freefeta.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ketch.DownloadModel
import et.fira.freefeta.data.file.FileDownloaderRepository
import et.fira.freefeta.data.file.LocalFileRepository
import et.fira.freefeta.data.file.RemoteFileRepository
import et.fira.freefeta.model.FileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val fileDownloaderRepository: FileDownloaderRepository,
    private val localFileRepository: LocalFileRepository,
    private val remoteFileRepository: RemoteFileRepository
): ViewModel() {
    init {
        viewModelScope.launch {
           val fileList =  remoteFileRepository.getFiles()
            if (fileList.isNotEmpty()) {
                for (file in fileList) {
                    localFileRepository.insertFile(file)
                }
            }
            Log.d("HomeViewModel", "inserted ${fileList.count()} files")
        }
    }
    private val filesFlow: Flow<List<FileEntity>> = localFileRepository.getAllFilesStream()
    private val downloadModelsFlow: Flow<List<DownloadModel>> = fileDownloaderRepository.observeDownloads()

    val uiState: StateFlow<HomeUiState> = combine(filesFlow, downloadModelsFlow) { files, downloads ->
        val itemList: List<DownloadItem> = files.map { file ->
            val download = downloads.find { it.id == file.downloadId }
            DownloadItem(file, download)
        }
        HomeUiState(itemList)
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        HomeUiState()
    )

    fun downloadFile(fileId: Int, url: String) {
        val downloadId = fileDownloaderRepository.download(url)
        viewModelScope.launch {
            localFileRepository.updateFileDownloadId(fileId, downloadId)
        }
    }

}

data class HomeUiState(
    val downloadItemList: List<DownloadItem> = listOf()
)

//data class HomeUiState(
//    val files: List<FileEntity> = listOf(),
//    val downloadModels: List<DownloadModel> = listOf()
//)

data class DownloadItem(
    val file: FileEntity,
    val downloadModel: DownloadModel?
)
