package et.fira.freefeta.data.file

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import et.fira.freefeta.model.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fileEntity: FileEntity)

    @Update
    suspend fun update(fileEntity: FileEntity)

    @Query("UPDATE files SET download_id = :newFileDownloadId WHERE id = :fileId")
    suspend fun updateFileDownloadId(fileId: Int, newFileDownloadId: Int)

    @Delete
    suspend fun delete(fileEntity: FileEntity)

    @Query("SELECT * from files WHERE id = :id")
    fun getFile(id: Int): Flow<FileEntity>

    @Query("SELECT * from files ORDER BY created_at DESC")
    fun getAllFiles(): Flow<List<FileEntity>>



}