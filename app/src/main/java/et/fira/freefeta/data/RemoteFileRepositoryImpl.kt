package et.fira.freefeta.data

import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.network.FreeFetaApiService

class RemoteFileRepositoryImpl(private val freeFetaApiService: FreeFetaApiService) : RemoteFileRepository {
    override suspend fun getFiles(): List<FileEntity> = freeFetaApiService.getFiles()
}