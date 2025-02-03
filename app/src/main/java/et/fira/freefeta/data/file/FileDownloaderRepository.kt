package et.fira.freefeta.data.file

import android.os.Environment
import com.ketch.DownloadModel
import et.fira.freefeta.network.TeleFileDownloaderService
import kotlinx.coroutines.flow.Flow

class FileDownloaderRepository(
    private val teleFileDownloaderService: TeleFileDownloaderService
) {
    fun download(url: String): Int {
        return teleFileDownloaderService.download(
            url,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
        )
    }

    fun observeDownloads(): Flow<List<DownloadModel>> {
        return teleFileDownloaderService.observeDownloads()
    }


}