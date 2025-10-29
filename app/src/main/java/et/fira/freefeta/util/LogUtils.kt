package et.fira.freefeta.util

import android.util.Log
import et.fira.freefeta.BuildConfig

object Logger {
    private const val TAG = "FreeFeta"

    fun d(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }

    fun w(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }
}