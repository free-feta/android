package et.fira.freefeta.network

import android.net.Uri
import android.webkit.URLUtil
import com.ketch.DownloadModel
import com.ketch.Ketch
import et.fira.freefeta.util.AppConstants
import kotlinx.coroutines.flow.Flow

class TeleFileDownloaderService(
    private val ketch: Ketch
) {
    fun download(
        url: String,
        path: String,
        fileName: String? = null,
        tag: String = "",
        headers: HashMap<String, String> = hashMapOf(),
        sendId: String? = null
    ): Int {
        val uri = Uri.parse(url)
        val finalHeaders = if (uri.host == "telebirrchat.ethiomobilemoney.et" && headers.isEmpty()) {
            val zeroRatingHeaders = HashMap(AppConstants.Network.HEADER_FOR_ZERO_RATING_URL
                .toMutableMap())
            if (sendId != null) {
                zeroRatingHeaders["sendid"] = sendId
            }
            zeroRatingHeaders
        } else {
            headers
        }

        val finalFileName = fileName
            ?: if (uri.host == "telebirrchat.ethiomobilemoney.et") {
                uri.getQueryParameter("filename") ?: URLUtil.guessFileName(url, null, null)
            } else {
                URLUtil.guessFileName(url, null, null)
            }
        return ketch.download(
            url = url,
            path = path,
            fileName= finalFileName,
            tag = tag,
            headers = finalHeaders

        )
    }

    fun cancel(id: Int) {
        ketch.cancel(id)
    }
//    fun cancel(tag: String) {
//        ketch.cancel(tag)
//    }
//    fun cancelAll() {
//        ketch.cancelAll()
//    }

    fun pause(id: Int) {
        ketch.pause(id)
    }
//    fun pause(tag: String) {
//        ketch.pause(tag)
//    }
//    fun pauseAll() {
//        ketch.pauseAll()
//    }

    fun resume(id: Int) {
        ketch.resume(id)
    }
//    fun resume(tag: String) {
//        ketch.resume(tag)
//    }
//    fun resumeAll() {
//        ketch.resumeAll()
//    }

    fun retry(id: Int) {
        ketch.retry(id)
    }
//    fun retry(tag: String) {
//        ketch.retry(tag)
//    }
//    fun retryAll() {
//        ketch.retryAll()
//    }

    fun delete(id: Int) {
        ketch.clearDb(id)
    }
    fun delete(id: Int, deleteFile: Boolean) {
        ketch.clearDb(id, deleteFile)
    }
    fun delete(tag: String) {
        ketch.clearDb(tag)
    }
    fun delete(tag: String, deleteFile: Boolean) {
        ketch.clearDb(tag, deleteFile)
    }
//    fun deleteAll() {
//        ketch.clearAllDb()
//    }

    fun observeDownloadById(id: Int): Flow<DownloadModel> {
        return ketch.observeDownloadById(id)
    }
    fun observeDownloads(): Flow<List<DownloadModel>> {
        return ketch.observeDownloads()
    }

}