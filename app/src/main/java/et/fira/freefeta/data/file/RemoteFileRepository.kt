package et.fira.freefeta.data.file

import et.fira.freefeta.model.FileEntity

interface RemoteFileRepository {
    suspend fun getFiles(): List<FileEntity>
}