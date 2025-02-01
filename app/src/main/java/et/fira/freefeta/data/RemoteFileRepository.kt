package et.fira.freefeta.data

import et.fira.freefeta.model.FileEntity

interface RemoteFileRepository {
    suspend fun getFiles(): List<FileEntity>
}