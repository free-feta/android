package et.fira.freefeta.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import et.fira.freefeta.model.Advertisement
import et.fira.freefeta.model.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(advertisement: Advertisement)

    @Update
    suspend fun update(advertisement: Advertisement)

    @Delete
    suspend fun delete(advertisement: Advertisement)

    @Query("SELECT * from advertisements WHERE id = :id")
    fun getAd(id: Int): Flow<Advertisement>

    @Query("SELECT * from advertisements")
    fun getAllAds(): Flow<List<Advertisement>>
}