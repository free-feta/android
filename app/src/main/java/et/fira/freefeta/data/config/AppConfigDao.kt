package et.fira.freefeta.data.config

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import et.fira.freefeta.model.AppConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: AppConfig)

    @Update
    suspend fun update(config: AppConfig)

    @Delete
    suspend fun delete(config: AppConfig)

    @Query("SELECT * from app_config WHERE id = 1")
    fun getConfigStream(): Flow<AppConfig?>

    @Query("SELECT * from app_config WHERE id = 1")
    fun getConfig(): AppConfig?

}