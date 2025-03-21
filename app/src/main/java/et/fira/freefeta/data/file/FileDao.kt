package et.fira.freefeta.data.file

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.model.FileType
import et.fira.freefeta.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(fileEntity: FileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(fileEntities: List<FileEntity>)

    @Update
    suspend fun updateFile(fileEntity: FileEntity)

    @Query("UPDATE files SET download_id = :newFileDownloadId, is_new = 0 WHERE id = :fileId")
    suspend fun updateFileDownloadId(fileId: Int, newFileDownloadId: Int?)

    @Delete
    suspend fun deleteFile(fileEntity: FileEntity)

    @Delete
    suspend fun deleteFile(fileEntities: List<FileEntity>)

    @Query("SELECT * from files WHERE id = :id")
    fun getFile(id: Int): Flow<FileEntity>

    @Query("SELECT * from files ORDER BY created_at DESC")
    suspend fun getAllFiles(): List<FileEntity>

    @Query("SELECT * from files ORDER BY created_at DESC")
    fun getAllFilesFlow(): Flow<List<FileEntity>>

    @Query("""
        SELECT * FROM files 
        WHERE (name LIKE '%' || :query || '%' OR folder_name LIKE '%' || :query || '%')
        AND (:fileType IS NULL OR file_type = :fileType)
        AND (:mediaType IS NULL OR media_type = :mediaType)
        ORDER BY name ASC
    """)
    fun searchFiles(
        query: String = "",
        fileType: FileType? = null,
        mediaType: MediaType? = null
    ): Flow<List<FileEntity>>

    @Query("SELECT DISTINCT file_type FROM files")
    fun getAllFileTypes(): Flow<List<FileType>>

    @Query("SELECT DISTINCT media_type FROM files WHERE media_type IS NOT NULL")
    fun getAllMediaTypes(): Flow<List<MediaType>>



}