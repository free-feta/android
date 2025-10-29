package et.fira.freefeta.util

import okhttp3.Interceptor
import okhttp3.Response

class RequestHeaderInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url.toString()

        if (originalUrl.contains("telebirrchat.ethiomobilemoney.et")) {
            val modifiedRequest = originalRequest.newBuilder()
            for (header in AppConstants.Network.ZERO_RATING_HEADER) {
                modifiedRequest.addHeader(header.key, header.value)
            }
            return chain.proceed(modifiedRequest.build())
        }

        return chain.proceed(originalRequest)
    }
}
