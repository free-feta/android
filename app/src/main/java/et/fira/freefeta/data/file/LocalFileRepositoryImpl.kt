package et.fira.freefeta.data.file

import et.fira.freefeta.model.FileEntity
import kotlinx.coroutines.flow.Flow

class LocalFileRepositoryImpl(
    private val fileDao: FileDao
): LocalFileRepository {
    override fun getAllFilesStream(): Flow<List<FileEntity>> = fileDao.getAllFiles()

    override fun getFileStream(id: Int): Flow<FileEntity?> = fileDao.getFile(id)

    override suspend fun insertFile(fileEntity: FileEntity) = fileDao.insert(fileEntity)

    override suspend fun deleteFile(fileEntity: FileEntity) = fileDao.delete(fileEntity)

    override suspend fun updateFile(fileEntity: FileEntity) = fileDao.update(fileEntity)
    override suspend fun updateFileDownloadId(fileId: Int, newFileDownloadId: Int) = fileDao.updateFileDownloadId(fileId, newFileDownloadId)

}