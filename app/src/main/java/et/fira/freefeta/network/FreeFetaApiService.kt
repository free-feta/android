package et.fira.freefeta.network

import et.fira.freefeta.model.FileEntity
import retrofit2.http.GET

interface FreeFetaApiService {
    @GET("files.json")
    suspend fun getFiles(): List<FileEntity>
}


