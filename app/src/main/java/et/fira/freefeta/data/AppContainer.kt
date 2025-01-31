package et.fira.freefeta.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ketch.Ketch
import com.ketch.NotificationConfig
import et.fira.freefeta.R
import et.fira.freefeta.network.TeleFileDownloaderService

private const val USER_PREFERENCE_NAME = "user_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCE_NAME
)

interface AppContainer {
    val fileDownloaderRepository: FileDownloaderRepository
    val mediaRepository: MediaRepository
    val userPreferencesRepository: UserPreferencesRepository
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

    override val mediaRepository: MediaRepository by lazy {
        OfflineMediaRepository(FreeFetaDatabase.getDatabase(context).mediaDao())
    }
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }

}