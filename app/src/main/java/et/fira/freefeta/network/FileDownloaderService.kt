package et.fira.freefeta.network

import android.net.Uri
import android.webkit.URLUtil
import com.ketch.DownloadModel
import com.ketch.Ketch
import kotlinx.coroutines.flow.Flow

class TeleFileDownloaderService(
    private val ketch: Ketch
) {
    fun download(
        url: String,
        path: String,
        fileName: String? = null,
        tag: String = "",
        headers: HashMap<String, String> = hashMapOf()
    ): Int {
        val uri = Uri.parse(url)
        val finalHeaders = if (uri.host == "telebirrchat.ethiomobilemoney.et" && headers.isEmpty()) {
            hashMapOf(
                "appid" to "1012673623603201",
                "access-token" to "34A1367993D9409639D081B3A91159D37FD5DF999E2B97C8C822F92E44DE6A65",
                "User-Agent" to "Dalvik/2.1.0 (Linux; U; Android 7.1.2; ASUS_Z01QD Build/N2G48H)",
                "sendid" to "1012673623603201:978019208678401",
            )
        } else {
            headers
        }

        val finalFileName = fileName
            ?: if (uri.host == "telebirrchat.ethiomobilemoney.et") {
                uri.getQueryParameter("filename") ?: "video.mp4"
            } else {
                URLUtil.guessFileName(url, null, null)
            }
        } else {
            fileName
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
    fun cancel(tag: String) {
        ketch.cancel(tag)
    }
    fun cancelAll() {
        ketch.cancelAll()
    }

    fun pause(id: Int) {
        ketch.pause(id)
    }
    fun pause(tag: String) {
        ketch.pause(tag)
    }
    fun pauseAll() {
        ketch.pauseAll()
    }

    fun resume(id: Int) {
        ketch.resume(id)
    }
    fun resume(tag: String) {
        ketch.resume(tag)
    }
    fun resumeAll() {
        ketch.resumeAll()
    }

    fun retry(id: Int) {
        ketch.retry(id)
    }
    fun retry(tag: String) {
        ketch.retry(tag)
    }
    fun retryAll() {
        ketch.retryAll()
    }

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
    fun deleteAll() {
        ketch.clearAllDb()
    }

    fun observeDownloadById(id: Int): Flow<DownloadModel> {
        return ketch.observeDownloadById(id)
    }
    fun observeDownloads(): Flow<List<DownloadModel>> {
        return ketch.observeDownloads()
    }

}