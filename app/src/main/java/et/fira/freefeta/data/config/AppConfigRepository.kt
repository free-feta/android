package et.fira.freefeta.data.config

import android.util.Log
import android.webkit.URLUtil
import et.fira.freefeta.model.AppConfig
import et.fira.freefeta.network.FreeFetaApiService
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.cancellation.CancellationException

interface AppConfigRepository {
    suspend fun insertConfig(config: AppConfig)
    suspend fun deleteConfig(config: AppConfig)
    fun getConfigStream(): Flow<AppConfig?>
    suspend fun syncConfig(): AppConfig?
    suspend fun getConfig(): AppConfig?
    suspend fun getAnalyticsUrl(): String?
}

class AppConfigRepositoryImpl(
    private val appConfigDao: AppConfigDao,
    private val freeFetaApiService: FreeFetaApiService
): AppConfigRepository {
    override suspend fun insertConfig(config: AppConfig) = appConfigDao.insert(config)

    override suspend fun deleteConfig(config: AppConfig) = appConfigDao.delete(config)

    override fun getConfigStream(): Flow<AppConfig?> = appConfigDao.getConfigStream()
    override suspend fun syncConfig(): AppConfig? {
        try {
            val fetchedConfig = freeFetaApiService.getConfig()
            Log.d("AppConfigRepository", "Fetched config: $fetchedConfig")
            insertConfig(fetchedConfig)
            Log.d("AppConfigRepository", "after insert in sync, thread: ${Thread.currentThread().name}")
            return fetchedConfig

        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            e.printStackTrace()
            return null
        }
    }
    override suspend fun getConfig(): AppConfig? {
        try {
            Log.d("AppConfigRepository", "before syncing, thread: ${Thread.currentThread().name}")
            syncConfig()
            return appConfigDao.getConfig()
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            e.printStackTrace()
            return null
        }
    }

    override suspend fun getAnalyticsUrl(): String? {
        try {
            Log.d("AppConfigRepository", "Fetching analytics URL, thread: ${Thread.currentThread().name}")
            val url = getConfig()?.analyticsUrl
            Log.d("AppConfigRepository", "Fetched analytics URL: $url")
            return if (URLUtil.isValidUrl(url)) {
                url
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            e.printStackTrace()
            return null
        }
    }


}