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
           val fetchedFiles =  remoteFileRepository.getFiles()
            // Get Stored files from db and
            // set downloadId to null to all FileEntity to compare with
            // fetchedFiles as it has no downloadId
            val storedFiles = localFileRepository.getAllFiles().map { it.copy(downloadId = null) }
            val newFiles = fetchedFiles.filter { fetchedFile ->
                storedFiles.none { storedFile -> fetchedFile == storedFile }
            }
            for (file in newFiles) {
                localFileRepository.insertFile(file)
            }
            Log.d("HomeViewModel", "inserted ${newFiles.count()} files")
//            Log.d("HomeViewModel", storedFiles.toString())
//            Log.d("HomeViewModel", fetchedFiles.toString())
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


data class DownloadItem(
    val file: FileEntity,
    val downloadModel: DownloadModel?
)
