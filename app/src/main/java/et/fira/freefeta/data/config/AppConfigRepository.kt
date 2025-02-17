package et.fira.freefeta.data.config

import android.util.Log
import et.fira.freefeta.model.AppConfig
import et.fira.freefeta.network.FreeFetaApiService
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.cancellation.CancellationException

interface AppConfigRepository {
    suspend fun insertConfig(config: AppConfig)
    suspend fun deleteConfig(config: AppConfig)
    fun getConfigStream(): Flow<AppConfig?>
    suspend fun syncConfig()
    suspend fun getConfig(): AppConfig?
}

class AppConfigRepositoryImpl(
    private val appConfigDao: AppConfigDao,
    private val freeFetaApiService: FreeFetaApiService
): AppConfigRepository {
    override suspend fun insertConfig(config: AppConfig) = appConfigDao.insert(config)

    override suspend fun deleteConfig(config: AppConfig) = appConfigDao.delete(config)

    override fun getConfigStream(): Flow<AppConfig?> = appConfigDao.getConfigStream()
    override suspend fun syncConfig() {
        try {
            val fetchedConfig = freeFetaApiService.getConfig()
            Log.d("AppConfigRepository", "Fetched config: $fetchedConfig")
            insertConfig(fetchedConfig)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            e.printStackTrace()
        }
    }
    override suspend fun getConfig(): AppConfig? {
        try {
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


}