package et.fira.freefeta.data

import et.fira.freefeta.model.AppConfig
import kotlinx.coroutines.flow.Flow

interface AppConfigRepository {
    suspend fun insertConfig(config: AppConfig)
    suspend fun deleteConfig(config: AppConfig)
    fun getConfig(): Flow<AppConfig>
}

class AppConfigRepositoryImpl(
    private val appConfigDao: AppConfigDao
): AppConfigRepository {
    override suspend fun insertConfig(config: AppConfig) = appConfigDao.insert(config)

    override suspend fun deleteConfig(config: AppConfig) = appConfigDao.delete(config)

    override fun getConfig(): Flow<AppConfig> = appConfigDao.getConfig()

}