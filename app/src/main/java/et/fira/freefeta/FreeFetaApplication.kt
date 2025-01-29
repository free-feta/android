package et.fira.freefeta

import android.app.Application
import et.fira.freefeta.data.AppContainer
import et.fira.freefeta.data.DefaultAppContainer

class FreeFetaApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}