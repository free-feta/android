package et.fira.freefeta

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import et.fira.freefeta.data.AppContainer
import et.fira.freefeta.data.DefaultAppContainer
import et.fira.freefeta.util.RequestHeaderInterceptor
import okhttp3.OkHttpClient

class FreeFetaApplication: Application(), SingletonImageLoader.Factory {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        container.workManagerSynUpdateRepository.startSyncUpdate()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(
                    callFactory = {
                        OkHttpClient.Builder()
                            .addNetworkInterceptor(RequestHeaderInterceptor())
                            .build()
                    }
                ))
            }
            .build()
    }
}