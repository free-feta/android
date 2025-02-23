package et.fira.freefeta.network

import et.fira.freefeta.model.Advertisement
import et.fira.freefeta.model.AppConfig
import et.fira.freefeta.model.FileEntity
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface FreeFetaApiService {
    @GET("files.json")
    suspend fun getFiles(): List<FileEntity>

    @GET("ads.json")
    suspend fun getAds(): List<Advertisement>

    @GET("config.json")
    suspend fun getConfig(): AppConfig

    @POST
    suspend fun uploadJsonData(
        @Url url: String,
        @Body requestBody: RequestBody
    ): ResponseBody?
}


