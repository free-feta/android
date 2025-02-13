package et.fira.freefeta.network

import et.fira.freefeta.model.Advertisement
import et.fira.freefeta.model.FileEntity
import retrofit2.http.GET

interface FreeFetaApiService {
    @GET("files.json")
    suspend fun getFiles(): List<FileEntity>

    @GET("ads.json")
    suspend fun getAds(): List<Advertisement>
}


