package et.fira.freefeta.util

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.data.file.LocalFileRepository
import et.fira.freefeta.data.file.RemoteFileRepository
import kotlinx.coroutines.ensureActive
import java.io.File
import kotlin.coroutines.coroutineContext

object Util {

    private const val MAX_PERCENT = 100
    private const val VALUE_60 = 60
    private const val VALUE_3 = 3
    private const val VALUE_300 = 300
    private const val VALUE_500 = 500
    private const val VALUE_1024 = 1024
    private const val SEC_IN_MILLIS = 1000

    fun isVersionOlder(installed: String, required: String): Boolean {
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

    suspend fun syncNewFilesAndClearGarbage(
        remoteFileRepository: RemoteFileRepository,
        localFileRepository: LocalFileRepository
    ): Int {
        try {
            val fetchedFiles =  remoteFileRepository.getFiles()

            // Get Stored files from db and
            // set downloadId to null to all FileEntity to compare with
            // fetchedFiles as it has no downloadId and isNew to true
            val storedFiles = localFileRepository.getAllFiles().map { it.copy(downloadId = null, isNew = true) }
            val newFiles = fetchedFiles.filter { fetchedFile ->
                storedFiles.none { storedFile -> fetchedFile == storedFile }
            }
            if (newFiles.isNotEmpty()) {
                localFileRepository.insertFile(newFiles)
            }
            val garbageFiles = storedFiles.filter { storedFile ->
                fetchedFiles.none { fetchedFile -> fetchedFile == storedFile }
            }
            if (garbageFiles.isNotEmpty()) {
                localFileRepository.deleteFile(garbageFiles)
            }
            Log.d("HomeViewModel", "Fetched ${newFiles.size} new files")
            return newFiles.size

        } catch (e: Exception) {
            coroutineContext.ensureActive()
            e.printStackTrace()
        }
        return 0
    }

    fun getTimeLeftText(
        speedInBPerMs: Float,
        progressPercent: Int,
        lengthInBytes: Long
    ): String {
        if (speedInBPerMs == 0F) return ""
        val speedInBPerSecond = speedInBPerMs * SEC_IN_MILLIS
        val bytesLeft = (lengthInBytes * (MAX_PERCENT - progressPercent) / MAX_PERCENT).toFloat()

        val secondsLeft = bytesLeft / speedInBPerSecond
        val minutesLeft = secondsLeft / VALUE_60
        val hoursLeft = minutesLeft / VALUE_60

        return when {
            secondsLeft < VALUE_60 -> "%.0f s left".format(secondsLeft)
            minutesLeft < VALUE_3 -> "%.0f mins %.0f s left".format(
                minutesLeft,
                secondsLeft % VALUE_60
            )

            minutesLeft < VALUE_60 -> "%.0f mins left".format(minutesLeft)
            minutesLeft < VALUE_300 -> "%.0f hrs %.0f mins left".format(
                hoursLeft,
                minutesLeft % VALUE_60
            )

            else -> "%.0f hrs left".format(hoursLeft)
        }
    }

    fun getSpeedText(speedInBPerMs: Float): String {
        var value = speedInBPerMs * SEC_IN_MILLIS
        val units = arrayOf("B/s", "KB/s", "MB/s", "GB/s")
        var unitIndex = 0

        while (value >= VALUE_500 && unitIndex < units.size - 1) {
            value /= VALUE_1024
            unitIndex++
        }

        return "%.2f %s".format(value, units[unitIndex])
    }

    fun getTotalLengthText(lengthInBytes: Long): String {
        var value = lengthInBytes.toFloat()
        val units = arrayOf("B", "KB", "MB", "GB")
        var unitIndex = 0

        while (value >= VALUE_500 && unitIndex < units.size - 1) {
            value /= VALUE_1024
            unitIndex++
        }

        return "%.2f %s".format(value, units[unitIndex])
    }

    fun getCompleteText(
        speedInBPerMs: Float,
        progress: Int,
        length: Long
    ): String {
        val timeLeftText = getTimeLeftText(speedInBPerMs, progress, length)
        val speedText = getSpeedText(speedInBPerMs)

        val parts = mutableListOf<String>()

        if (timeLeftText.isNotEmpty()) {
            parts.add(timeLeftText)
        }

        if (speedText.isNotEmpty()) {
            parts.add(speedText)
        }

        return parts.joinToString(", ")
    }

}

fun Context.hasFilePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        val readPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PermissionChecker.PERMISSION_GRANTED

        val writePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PermissionChecker.PERMISSION_GRANTED

        readPermission && writePermission
    }
}


fun createAndCheckFolderLegacy(context: Context, folderName: String): Boolean {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appFolder = File(downloadsDir, folderName)

        // Check if folder exists
        if (!appFolder.exists()) {
            // Create folder if it doesn't exist
            val success = appFolder.mkdirs()
            if (!success) {
                return false
            }
        }

        // Verify folder exists and is a directory
        return appFolder.exists() && appFolder.isDirectory

    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

//@RequiresApi(Build.VERSION_CODES.Q)
//fun createAndCheckFolderApi29(context: Context, folderName: String): Boolean {
//    try {
//        val relativePath = Environment.DIRECTORY_DOWNLOADS + File.separator + folderName
//
//        // Create folder using MediaStore
//        val values = ContentValues().apply {
//            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
//            put(MediaStore.MediaColumns.DISPLAY_NAME, ".nomedia")
//            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
//        }
//
//        context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
//
//        // Verify folder exists
//        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName)
//        return folder.exists() && folder.isDirectory
//
//    } catch (e: Exception) {
//        e.printStackTrace()
//        return false
//    }
//}

@RequiresApi(Build.VERSION_CODES.Q)
fun createAndCheckFolderApi29(context: Context, folderName: String): Boolean {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetFolder = File(downloadsDir, folderName)

        // First check if folder exists in filesystem
        if (!targetFolder.exists() || !targetFolder.isDirectory) {
            // Create through MediaStore only if it doesn't exist
            createFolderViaMediaStore(context, folderName)
        }

        // Double-check existence
        targetFolder.exists() && targetFolder.isDirectory
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun createFolderViaMediaStore(context: Context, folderName: String) {
    val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$folderName"

    // Check if .nomedia file already exists in MediaStore
    if (!doesNomediaFileExist(context, relativePath)) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.DISPLAY_NAME, ".nomedia")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        )

        // Mark as complete if successfully created
        uri?.let {
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun doesNomediaFileExist(context: Context, relativePath: String): Boolean {
    val projection = arrayOf(MediaStore.MediaColumns._ID)
    val selection = """
        ${MediaStore.MediaColumns.RELATIVE_PATH} = ? 
        AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?
    """.trimIndent()

    val selectionArgs = arrayOf(relativePath, ".nomedia")

    return context.contentResolver.query(
        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        cursor.count > 0
    } ?: false
}

// Combined approach for all Android versions
fun createAndCheckFolderCompat(context: Context, folderName: String = AppConstants.File.DOWNLOAD_FOLDER_NAME): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        createAndCheckFolderApi29(context, folderName)
    } else {
        createAndCheckFolderLegacy(context, folderName)
    }
}

fun Context.createAndCheckFolder(folderName: String = AppConstants.File.DOWNLOAD_FOLDER_NAME): Boolean = createAndCheckFolderCompat(this, folderName)

fun Context.openFile(downloadModel: DownloadModel) {
    // 1. Check Download Status: Early Exit
    if (downloadModel.status != Status.SUCCESS) {
        Toast.makeText(this, "File not downloaded yet", Toast.LENGTH_SHORT).show()
        return
    }

    // 2. Construct File Path: Use Path.Combine
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (downloadsDir == null) {
        Toast.makeText(this, "Downloads directory not accessible", Toast.LENGTH_SHORT).show()
        return
    }
    val file = File("${downloadModel.path}/${downloadModel.fileName}")

    // 3. Check File Existence: Early Exit
    if (!file.exists()) {
        Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
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
            this,
            "${this.packageName}.provider", // Must match manifest declaration
            file
        )
//            }
    } catch (e: IllegalArgumentException) {
        Toast.makeText(this, "File not accessible", Toast.LENGTH_SHORT).show()
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
        this.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show()
    } catch (e: SecurityException) {
        Toast.makeText(this, "Security error: Cannot access file", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
    }
}

fun Context.openFolder(folderName: String? = null) {
    val context = this
    val folderPath = "${Environment.DIRECTORY_DOWNLOADS}/${AppConstants.File.DOWNLOAD_FOLDER_NAME}" +
            if (folderName != null) "/$folderName" else ""

    val file = File(Environment.getExternalStorageDirectory(), folderPath)
    // 3. Check Folder Existence: Early Exit
    if (!file.exists()) {
        Toast.makeText(context, "Folder not found", Toast.LENGTH_SHORT).show()
        return
    }

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
