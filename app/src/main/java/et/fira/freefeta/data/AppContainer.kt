package et.fira.freefeta.data

import android.content.Context
import com.ketch.Ketch
import com.ketch.NotificationConfig
import et.fira.freefeta.R
import et.fira.freefeta.network.TeleFileDownloaderService

interface AppContainer {
    val fileDownloaderRepository: FileDownloaderRepository
}

class DefaultAppContainer(context: Context): AppContainer {
    private val ketch = Ketch.builder()
        .setNotificationConfig(
            config = NotificationConfig(
                enabled = true,
                smallIcon = R.drawable.mythemedicon
            )
        )
        .build(context)

    private val teleFileDownloaderService = TeleFileDownloaderService(ketch)

    override val fileDownloaderRepository: FileDownloaderRepository by lazy {
        MediaFileDownloaderRepository(teleFileDownloaderService)
    }

}