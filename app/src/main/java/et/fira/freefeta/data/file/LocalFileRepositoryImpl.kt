package et.fira.freefeta.data.file

import et.fira.freefeta.model.FileEntity
import kotlinx.coroutines.flow.Flow

class LocalFileRepositoryImpl(
    private val fileDao: FileDao
): LocalFileRepository {
    override fun getAllFilesStream(): Flow<List<FileEntity>> = fileDao.getAllFilesFlow()
    override suspend fun getAllFiles(): List<FileEntity> = fileDao.getAllFiles()

    override fun getFileStream(id: Int): Flow<FileEntity?> = fileDao.getFile(id)

    override suspend fun insertFile(fileEntity: FileEntity) = fileDao.insertFile(fileEntity)
    override suspend fun insertFile(fileEntities: List<FileEntity>) = fileDao.insertFile(fileEntities)

    override suspend fun deleteFile(fileEntity: FileEntity) = fileDao.deleteFile(fileEntity)
    override suspend fun deleteFile(fileEntities: List<FileEntity>) = fileDao.deleteFile(fileEntities)

    override suspend fun updateFile(fileEntity: FileEntity) = fileDao.updateFile(fileEntity)
    override suspend fun updateFileDownloadId(fileId: Int, newFileDownloadId: Int?) = fileDao.updateFileDownloadId(fileId, newFileDownloadId)

}