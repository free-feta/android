package et.fira.freefeta.data.file

import et.fira.freefeta.model.FileEntity
import kotlinx.coroutines.flow.Flow

interface LocalFileRepository {
    /**
     * Retrieve all the files as flow from the the given data source.
     */
    fun getAllFilesStream(): Flow<List<FileEntity>>

    /**
     * Retrieve all the files from the the given data source.
     */
    suspend fun getAllFiles(): List<FileEntity>

    /**
     * Retrieve an file from the given data source that matches with the [id].
     */
    fun getFileStream(id: Int): Flow<FileEntity?>

    /**
     * Insert file in the data source
     */
    suspend fun insertFile(fileEntity: FileEntity)

    /**
     * Insert list of life into the data source
     */
    suspend fun insertFile(fileEntities: List<FileEntity>)

    /**
     * Delete file from the data source
     */
    suspend fun deleteFile(fileEntity: FileEntity)
    suspend fun deleteFile(fileEntities: List<FileEntity>)

    /**
     * Update file in the data source
     */
    suspend fun updateFile(fileEntity: FileEntity)

    suspend fun updateFileDownloadId(fileId: Int, newFileDownloadId: Int?)
}