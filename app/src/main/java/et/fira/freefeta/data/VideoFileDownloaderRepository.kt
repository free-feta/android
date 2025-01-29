package et.fira.freefeta.data

import android.os.Environment
import et.fira.freefeta.network.TeleFileDownloaderService

class VideoFileDownloaderRepository(
    private val teleFileDownloaderService: TeleFileDownloaderService
): FileDownloaderRepository {
    override fun download(url: String): Int {
        return teleFileDownloaderService.download(
            url,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
        )
    }


}