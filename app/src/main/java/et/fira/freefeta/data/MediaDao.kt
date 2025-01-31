package et.fira.freefeta.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import et.fira.freefeta.model.Media
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: Media)

    @Update
    suspend fun update(media: Media)

    @Delete
    suspend fun delete(media: Media)

    @Query("SELECT * from medias WHERE id = :id")
    fun getMedia(id: Int): Flow<Media>

    @Query("SELECT * from medias ORDER BY created_at DESC")
    fun getAllItems(): Flow<List<Media>>

}