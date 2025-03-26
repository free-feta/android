package et.fira.freefeta.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ketch.Ketch
import com.ketch.NotificationConfig
import et.fira.freefeta.R
import et.fira.freefeta.data.ad.AdRepository
import et.fira.freefeta.data.ad.AdRepositoryImpl
import et.fira.freefeta.data.analytics.DeviceAnalytics
import et.fira.freefeta.data.analytics.DeviceAnalyticsRepo
import et.fira.freefeta.data.config.AppConfigRepository
import et.fira.freefeta.data.config.AppConfigRepositoryImpl
import et.fira.freefeta.data.file.FileDownloaderRepository
import et.fira.freefeta.data.file.LocalFileRepository
import et.fira.freefeta.data.file.LocalFileRepositoryImpl
import et.fira.freefeta.data.file.RemoteFileRepository
import et.fira.freefeta.data.file.RemoteFileRepositoryImpl
import et.fira.freefeta.network.FreeFetaApiService
import et.fira.freefeta.network.TeleFileDownloaderService
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

private const val USER_PREFERENCE_NAME = "user_preferences"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCE_NAME
)

interface AppContainer {
    val fileDownloaderRepository: FileDownloaderRepository
    val localFileRepository: LocalFileRepository
    val remoteFileRepository: RemoteFileRepository
    val appConfigRepository: AppConfigRepository
    val adRepository: AdRepository
    val userPreferencesRepository: UserPreferencesRepository
    val deviceAnalyticsRepo: DeviceAnalyticsRepo
    val workManagerSynUpdateRepository: SyncUpdateRepository

}

class DefaultAppContainer(context: Context) : AppContainer {
    private val ketch = Ketch.builder()
        .setNotificationConfig(
            config = NotificationConfig(
                enabled = true,
                smallIcon = R.drawable.mythemedicon
            )
        )
        .build(context)

    private val baseUrl =
        "https://raw.githubusercontent.com/fira-pro/json-store/refs/heads/main/free-feta/"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cache(
            Cache(
                directory = File(context.cacheDir, "http_cache"),
                maxSize = 50L * 1024L * 1024L // 50 MiB
            )
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .client(client)
        .build()

    private val retrofitService: FreeFetaApiService by lazy {
        retrofit.create(FreeFetaApiService::class.java)
    }

    private val teleFileDownloaderService = TeleFileDownloaderService(ketch)

    override val fileDownloaderRepository: FileDownloaderRepository by lazy {
        FileDownloaderRepository(teleFileDownloaderService)
    }

    override val localFileRepository: LocalFileRepository by lazy {
        LocalFileRepositoryImpl(FreeFetaDatabase.getDatabase(context).fileDao())
    }
    override val remoteFileRepository: RemoteFileRepository by lazy {
        RemoteFileRepositoryImpl(retrofitService)
    }
    override val appConfigRepository: AppConfigRepository by lazy {
        AppConfigRepositoryImpl(
            appConfigDao = FreeFetaDatabase.getDatabase(context).appConfigDao(),
            freeFetaApiService = retrofitService
        )
    }
    override val adRepository: AdRepository by lazy {
        AdRepositoryImpl(
            FreeFetaDatabase.getDatabase(context).adDao(),
            freeFetaApiService = retrofitService
        )
    }
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }
    override val deviceAnalyticsRepo: DeviceAnalyticsRepo = DeviceAnalyticsRepo(
        analytics = DeviceAnalytics(context),
        freeFetaApiService = retrofitService,
        appConfigRepository = appConfigRepository
    )
    override val workManagerSynUpdateRepository: SyncUpdateRepository =
        WorkManagerSynUpdateRepository(context)


}