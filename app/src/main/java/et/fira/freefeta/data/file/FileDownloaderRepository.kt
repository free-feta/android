package et.fira.freefeta.data.file

import android.os.Environment
import com.ketch.DownloadModel
import et.fira.freefeta.network.TeleFileDownloaderService
import et.fira.freefeta.util.AppConstants
import kotlinx.coroutines.flow.Flow
import java.io.File

class FileDownloaderRepository(
    private val teleFileDownloaderService: TeleFileDownloaderService
) {
    fun download(url: String, sendId: String? = null, folderName: String? = null): Int {
        val path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).path + File.separator + AppConstants.File.DOWNLOAD_FOLDER_NAME +
                if (folderName != null) {File.separator + folderName} else ""

        return teleFileDownloaderService.download(
            url = url,
            path = path,
            sendId = sendId
        )
    }

    fun cancelDownload(id: Int) = teleFileDownloaderService.cancel(id)

    fun pauseDownload(id: Int) = teleFileDownloaderService.pause(id)

    fun retryDownload(id: Int) = teleFileDownloaderService.retry(id)

    fun deleteDownload(id: Int) = teleFileDownloaderService.delete(id)

    fun resumeDownload(id: Int) = teleFileDownloaderService.resume(id)

    fun observeDownloads(): Flow<List<DownloadModel>> {
        return teleFileDownloaderService.observeDownloads()
    }
    fun observeDownload(id: Int): Flow<DownloadModel> {
        return teleFileDownloaderService.observeDownloadById(id)
    }



}