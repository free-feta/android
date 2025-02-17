package et.fira.freefeta.ui.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ketch.DownloadModel
import et.fira.freefeta.MainActivity
import et.fira.freefeta.data.config.AppConfigRepository
import et.fira.freefeta.data.file.FileDownloaderRepository
import et.fira.freefeta.model.AppConfig
import et.fira.freefeta.util.createAndCheckFolder
import et.fira.freefeta.util.hasFilePermission
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class UpdateViewModel(
    private val configRepository: AppConfigRepository,
    private val fileDownloaderRepository: FileDownloaderRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()
    private var updateDownloaderJob: Job? = null
    private var checkForDownloaderJob: Job? = null

    fun checkForUpdate(context: Context) {
        checkForDownloaderJob?.cancel()
        checkForDownloaderJob = viewModelScope.launch {
            configRepository.syncConfig()
            configRepository.getConfigStream().collect {config ->

                config?.let { appConfig ->
                    _uiState.value = _uiState.value.copy(config = appConfig)
                    val installedVersion = context.getInstalledVersion()

                    when {
                        isVersionOlder(installedVersion, config.minimumVersion) -> {
                            _uiState.value =
                                _uiState.value.copy(showUpdateDialog = true, forceUpdate = true)
                        }
                        isVersionOlder(installedVersion, config.latestVersion) -> {
                            _uiState.value =
                                _uiState.value.copy(showUpdateDialog = true, forceUpdate = false)
                        }
                        !config.isServiceOk -> {
                            _uiState.value = _uiState.value.copy(showUpdateDialog = true, forceUpdate = false)
                        }
                        else -> _uiState.value = _uiState.value.copy(showUpdateDialog = false)
                    }
                }
            }
        }
    }

    fun dismissDialog() {
        if (!_uiState.value.forceUpdate) {
            _uiState.value = _uiState.value.copy(showUpdateDialog = false)
        }
    }

    fun downloadUpdate(context: Context, url: String) {
        if (context.hasFilePermission()) {
            if (context.createAndCheckFolder()) {
                val downloadId = fileDownloaderRepository.download(url)
                updateDownloaderJob?.cancel()
                updateDownloaderJob = viewModelScope.launch {
                    fileDownloaderRepository.observeDownload(downloadId).collect { downloadModel ->
                        _uiState.value = _uiState.value.copy(downloadModel = downloadModel)
                    }
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

    fun retryDownload() {
        if (_uiState.value.downloadModel != null) {
            fileDownloaderRepository.retryDownload(_uiState.value.downloadModel!!.id)
        }
    }

    fun cancelDownload() {
        if (_uiState.value.downloadModel != null) {
            fileDownloaderRepository.cancelDownload(_uiState.value.downloadModel!!.id)
        }
    }

    fun openUpdateLink(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        val chooser = Intent.createChooser(intent, "Open with:")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "No app found to open the link.", Toast.LENGTH_SHORT).show()
        }
    }

    fun installApk(context: Context, apkFilePath: String) {
        val apkFile = File(apkFilePath) // Convert string path to File object

        if (!apkFile.exists()) {
            Toast.makeText(context, "Downloaded APK not found", Toast.LENGTH_SHORT).show()
            return
        }

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Check if the device allows installation from unknown sources


        // Start APK installation
        try {
            context.startActivity(intent)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if (!context.packageManager.canRequestPackageInstalls()) {
//                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
//                        data = Uri.parse("package:${context.packageName}")
//                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    }
//                    context.startActivity(settingsIntent)
//                    Toast.makeText(context, "Enable 'Install unknown apps' permission", Toast.LENGTH_LONG).show()
//                    return
//                }
//            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to start APK installation", Toast.LENGTH_SHORT).show()
        }
    }


    private fun isVersionOlder(installed: String, required: String): Boolean {
        val installedParts = installed.split('.').map { it.toIntOrNull() ?: 0 }
        val requiredParts = required.split('.').map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(installedParts.size, requiredParts.size)) {
            val installedValue = installedParts.getOrElse(i) { 0 }
            val requiredValue = requiredParts.getOrElse(i) { 0 }

            if (installedValue < requiredValue) return true
            if (installedValue > requiredValue) return false
        }
        return false
    }
}

data class UpdateUiState(
    val showUpdateDialog: Boolean = false,
    val forceUpdate: Boolean = false,
    val config: AppConfig? = null,
    val downloadModel: DownloadModel? = null,
)

fun Context.getInstalledVersion(): String {
    return try {
        val packageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
        packageInfo.versionName ?: "0.0.0" // Provide a default value if null
    } catch (e: PackageManager.NameNotFoundException) {
        "0.0.0"
    }
}