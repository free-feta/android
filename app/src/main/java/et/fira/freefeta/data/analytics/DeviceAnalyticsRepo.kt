package et.fira.freefeta.data.analytics

import et.fira.freefeta.data.config.AppConfigRepository
import et.fira.freefeta.network.FreeFetaApiService
import et.fira.freefeta.util.Logger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class DeviceAnalyticsRepo(
    private val analytics: DeviceAnalytics,
    private val freeFetaApiService: FreeFetaApiService,
    private val appConfigRepository: AppConfigRepository
) {
    suspend fun sendAnalytics() {
        Logger.d("DeviceAnalyticsRepo", "Sending analytics: $analytics")
        val url = appConfigRepository.getAnalyticsUrl() ?: return
        val deviceInfoJsonString = analytics.toJson()
        val requestBody = deviceInfoJsonString.toRequestBody("application/json".toMediaTypeOrNull())

        val response = freeFetaApiService.uploadJsonData(
            url = url,
            requestBody = requestBody)
        Logger.d("DeviceAnalyticsRepo", "Response: ${response?.string()}")

    }
}