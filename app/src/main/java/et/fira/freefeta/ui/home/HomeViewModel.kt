package et.fira.freefeta.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.data.UserPreferencesRepository
import et.fira.freefeta.data.file.FileDownloaderRepository
import et.fira.freefeta.data.file.LocalFileRepository
import et.fira.freefeta.data.file.RemoteFileRepository
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.util.AppConstants
import et.fira.freefeta.util.Util.syncNewFilesAndClearGarbage
import et.fira.freefeta.util.createAndCheckFolder
import et.fira.freefeta.util.hasFilePermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

sealed interface DownloadAction {
    data class Cancel(var downloadModel: DownloadModel): DownloadAction
    data class Pause(var downloadModel: DownloadModel): DownloadAction
    data class Retry(var downloadModel: DownloadModel): DownloadAction
    data class Resume(var downloadModel: DownloadModel): DownloadAction
    data class Download(var context: Context, var file: FileEntity): DownloadAction
    data class Open(var context: Context, var downloadModel: DownloadModel): DownloadAction
    data class OpenFolder(var context: Context): DownloadAction
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Fetched $newFiles new files", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    suspend  fun fetchNewFiles(): Int {
        return syncNewFilesAndClearGarbage(remoteFileRepository, localFileRepository)
    }

    //TODO: pass file name to downloader - [FileEntity.name]
    private fun downloadFile(context: Context, file: FileEntity) {
        if (context.hasFilePermission()) {
            if (context.createAndCheckFolder()) {
                val downloadId = fileDownloaderRepository.download(file.downloadUrl)
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
            is DownloadAction.Open -> openFile(downloadAction.context, downloadAction.downloadModel)
            is DownloadAction.OpenFolder -> openFolder(downloadAction.context)
            is DownloadAction.DeleteRequest -> onDeleteRequest(downloadAction.file.id, downloadAction.downloadModel.id)
            is DownloadAction.ConfirmDelete -> confirmDelete(downloadAction.neverShowAgain)
            is DownloadAction.DismissDelete -> dismissDialog()
            is DownloadAction.Play -> downloadAction.onPlay()
        }
    }

    // In your ViewModel or composable
    private fun openFile(context: Context, downloadModel: DownloadModel) {
        // 1. Check Download Status: Early Exit
        if (downloadModel.status != Status.SUCCESS) {
            Toast.makeText(context, "File not downloaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Construct File Path: Use Path.Combine
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir == null) {
            Toast.makeText(context, "Downloads directory not accessible", Toast.LENGTH_SHORT).show()
            return
        }
        val file = File(downloadsDir, "${AppConstants.File.DOWNLOAD_FOLDER_NAME}/${downloadModel.fileName}")

        // 3. Check File Existence: Early Exit
        if (!file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. Get Content URI: Handle Null
        val contentUri: Uri = try {
//            if (Build.VERSION.SDK_INT >= 29) {
//                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL).buildUpon()
//                    .appendPath(AppConstants.File.DOWNLOAD_FOLDER_NAME)
//                    .appendPath(downloadModel.fileName)
//                    .build()
//            } else {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider", // Must match manifest declaration
                    file
                )
//            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(context, "File not accessible", Toast.LENGTH_SHORT).show()
            return
        }

        // 5. Determine MIME Type: Use MimeTypeMap
        val mimeType = getMimeType(file) ?: "application/octet-stream" // Default to all types if unknown

        // 6. Create Intent: Use apply for clarity
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 7. Start Activity: Handle Exceptions More Granularity
        try {
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, "Security error: Cannot access file", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFolder(context: Context) {
        val folderPath = "${Environment.DIRECTORY_DOWNLOADS}/${AppConstants.File.DOWNLOAD_FOLDER_NAME}"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            if (Build.VERSION.SDK_INT >= 24) {
                // SAF for Android 7+
                val uri = DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "primary:$folderPath"
                )
                setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR)
            } else {
                // Legacy FileProvider for Android 5-6
                val file = File(Environment.getExternalStorageDirectory(), folderPath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                setDataAndType(uri, "resource/folder")
            }
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Open folder with"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No file manager installed", Toast.LENGTH_SHORT).show()
        }
    }


    // 8. Helper Function for MIME Type
    private fun getMimeType(file: File): String? {
        val extension = file.extension.lowercase() // Ensure lowercase for consistency
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }



}

data class HomeUiState(
    val downloadItemList: List<DownloadItem> = listOf()
)

data class DownloadItem(
    val file: FileEntity,
    val downloadModel: DownloadModel?,
)

data class DeleteFileInfo(
    val fileId: Int,
    val downloadId: Int
)
