package et.fira.freefeta.data.file

import et.fira.freefeta.model.FileEntity
import kotlinx.coroutines.flow.Flow

interface LocalFileRepository {
    /**
     * Retrieve all the medias from the the given data source.
     */
    fun getAllFilesStream(): Flow<List<FileEntity>>

    /**
     * Retrieve an media from the given data source that matches with the [id].
     */
    fun getFileStream(id: Int): Flow<FileEntity?>

    /**
     * Insert media in the data source
     */
    suspend fun insertFile(fileEntity: FileEntity)

    /**
     * Delete media from the data source
     */
    suspend fun deleteFile(fileEntity: FileEntity)

    /**
     * Update media in the data source
     */
    suspend fun updateFile(fileEntity: FileEntity)
}