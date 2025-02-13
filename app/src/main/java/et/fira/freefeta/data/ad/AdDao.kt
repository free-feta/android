package et.fira.freefeta.data.ad

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import et.fira.freefeta.model.Advertisement
import kotlinx.coroutines.flow.Flow

@Dao
interface AdDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(advertisement: Advertisement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(advertisements: List<Advertisement>)

    @Update
    suspend fun update(advertisement: Advertisement)

    @Delete
    suspend fun delete(advertisement: Advertisement)

      @Delete
    suspend fun delete(advertisements: List<Advertisement>)

    @Query("SELECT * from advertisements WHERE id = :id")
    fun getAd(id: Int): Flow<Advertisement>

    @Query("SELECT * from advertisements")
    fun getAllAdsStream(): Flow<List<Advertisement>>

    @Query("SELECT * from advertisements")
    suspend fun getAllAds(): List<Advertisement>
}