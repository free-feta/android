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
    fun download(url: String): Int {
        return teleFileDownloaderService.download(
            url,
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).path + File.separator + AppConstants.File.DOWNLOAD_FOLDER_NAME,
        )
    }

    fun observeDownloads(): Flow<List<DownloadModel>> {
        return teleFileDownloaderService.observeDownloads()
    }


}