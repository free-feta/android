package et.fira.freefeta.data.file

import android.os.Environment
import et.fira.freefeta.network.TeleFileDownloaderService

class FileDownloaderRepository(
    private val teleFileDownloaderService: TeleFileDownloaderService
) {
    fun download(url: String): Int {
        return teleFileDownloaderService.download(
            url,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
        )
    }


}