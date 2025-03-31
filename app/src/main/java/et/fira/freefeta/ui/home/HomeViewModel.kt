package et.fira.freefeta.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ketch.DownloadModel
import et.fira.freefeta.data.UserPreferencesRepository
import et.fira.freefeta.data.file.FileDownloaderRepository
import et.fira.freefeta.data.file.LocalFileRepository
import et.fira.freefeta.data.file.RemoteFileRepository
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.util.AppConstants
import et.fira.freefeta.util.Util.syncNewFilesAndClearGarbage
import et.fira.freefeta.util.createAndCheckFolder
import et.fira.freefeta.util.hasFilePermission
import et.fira.freefeta.util.openFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface DownloadAction {
    data class Cancel(var downloadModel: DownloadModel): DownloadAction
    data class Pause(var downloadModel: DownloadModel): DownloadAction
    data class Retry(var downloadModel: DownloadModel): DownloadAction
    data class Resume(var downloadModel: DownloadModel): DownloadAction
    data class Download(var context: Context, var file: FileEntity): DownloadAction
    data class Open(var onOpen: () -> Unit): DownloadAction
    data class OpenFolder(var context: Context, var folderName: String?): DownloadAction
    data class DeleteRequest(var downloadModel: DownloadModel, var file: FileEntity): DownloadAction
    data class ConfirmDelete(var neverShowAgain: Boolean): DownloadAction
    data object DismissDelete: DownloadAction
    data class Play(var onPlay: () -> Unit): DownloadAction
}

class HomeViewModel(
    private val fileDownloaderRepository: FileDownloaderRepository,
    private val localFileRepository: LocalFileRepository,
    private val remoteFileRepository: RemoteFileRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {
    private val showDeleteDialogPreferenceState: MutableState<Boolean> = mutableStateOf(false)

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private var currentDeleteFileInfo: DeleteFileInfo? = null

    init {
        viewModelScope.launch {
            userPreferencesRepository.showDeleteDialog.collect {
                showDeleteDialogPreferenceState.value = it
            }
        }
    }

    private fun onDeleteRequest(fileId: Int, downloadId: Int) {
        currentDeleteFileInfo = DeleteFileInfo(fileId, downloadId)
        if (showDeleteDialogPreferenceState.value) {
            _showDeleteDialog.value = true
        } else {
            deleteFileDownload(fileId, downloadId)
        }
    }

    private fun confirmDelete(neverShowAgain: Boolean) {
        viewModelScope.launch {
            currentDeleteFileInfo?.let { deleteInfo ->
                if (neverShowAgain) {
                    userPreferencesRepository.setShowDeleteDialog(false)
                }
                deleteFileDownload(deleteInfo.fileId, deleteInfo.downloadId)
                _showDeleteDialog.value = false
                currentDeleteFileInfo = null // Clear after deletion
            }
        }
    }

    private fun dismissDialog() {
        _showDeleteDialog.value = false
        currentDeleteFileInfo = null // Clear on dismiss
    }

    fun fetchNewFilesAndNotify(context: Context? = null) {
        viewModelScope.launch {
            val newFiles = fetchNewFiles()
            if (newFiles > 0) {
                Toast.makeText(context, "Fetched $newFiles new files", Toast.LENGTH_SHORT).show()
            }
        }

    }

    suspend  fun fetchNewFiles(): Int {
        return withContext(Dispatchers.IO) {
            syncNewFilesAndClearGarbage(remoteFileRepository, localFileRepository)
        }
    }

    //TODO: pass file name to downloader - [FileEntity.name]
    private fun downloadFile(context: Context, file: FileEntity) {
        if (context.hasFilePermission()) {
            if (context.createAndCheckFolder() &&
                (file.folderName == null || context.createAndCheckFolder(
                    AppConstants.File.DOWNLOAD_FOLDER_NAME + File.separator + file.folderName))) {
                val downloadId = fileDownloaderRepository.download(file.downloadUrl, file.sendid, file.folderName)
                viewModelScope.launch {
                    localFileRepository.updateFileDownloadId(file.id, downloadId)
                }

            } else {
                Toast.makeText(context, "Failed to create downloader folder", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please grant storage permission", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    }

    private fun deleteFileDownload(fileId: Int, downloadId: Int) {
        viewModelScope.launch {
            localFileRepository.updateFileDownloadId(fileId, null)
            fileDownloaderRepository.deleteDownload(downloadId)
        }
    }

    fun downloadAction(
        downloadAction: DownloadAction,
    ) {
        when(downloadAction) {
            is DownloadAction.Cancel -> fileDownloaderRepository.cancelDownload(downloadAction.downloadModel.id)
            is DownloadAction.Pause -> fileDownloaderRepository.pauseDownload(downloadAction.downloadModel.id)
            is DownloadAction.Retry -> fileDownloaderRepository.retryDownload(downloadAction.downloadModel.id)
            is DownloadAction.Resume -> fileDownloaderRepository.resumeDownload(downloadAction.downloadModel.id)
            is DownloadAction.Download -> downloadFile(downloadAction.context, downloadAction.file)
            is DownloadAction.Open -> downloadAction.onOpen()
            is DownloadAction.OpenFolder -> downloadAction.context.openFolder(downloadAction.folderName)
            is DownloadAction.DeleteRequest -> onDeleteRequest(downloadAction.file.id, downloadAction.downloadModel.id)
            is DownloadAction.ConfirmDelete -> confirmDelete(downloadAction.neverShowAgain)
            is DownloadAction.DismissDelete -> dismissDialog()
            is DownloadAction.Play -> downloadAction.onPlay()
        }
    }
}

data class DeleteFileInfo(
    val fileId: Int,
    val downloadId: Int
)
