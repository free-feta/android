package et.fira.freefeta.workers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import et.fira.freefeta.FreeFetaApplication
import et.fira.freefeta.ui.update.getInstalledVersion
import et.fira.freefeta.util.AppConstants
import et.fira.freefeta.util.Logger
import et.fira.freefeta.util.Util.isVersionOlder
import et.fira.freefeta.util.Util.syncNewFilesAndClearGarbage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "SyncUpdateWorker"

class SyncUpdateWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {

        return withContext(Dispatchers.IO) {
            return@withContext try {
                if (isInternetAvailable(applicationContext)) {
                    val remoteFileRepository = (applicationContext as FreeFetaApplication).container.remoteFileRepository
                    val localFileRepository = (applicationContext as FreeFetaApplication).container.localFileRepository
                    val appConfigRepository = (applicationContext as FreeFetaApplication).container.appConfigRepository
                    val adRepository = (applicationContext as FreeFetaApplication).container.adRepository

                    val newFilesCount = syncNewFilesAndClearGarbage(remoteFileRepository, localFileRepository)
                    if (newFilesCount > 0 ) {
                        makeUpdateNotification(
                            title = AppConstants.Worker.NEW_FILE_RELEASE_NOTIFICATION_TITLE,
                            message = "$newFilesCount new files released",
                            context = applicationContext
                        )
                    }

                    val config = appConfigRepository.syncConfig()
                    if (config != null) {
                        val msg: String? = if (isVersionOlder(applicationContext.getInstalledVersion(), config.minimumVersion)) {
                            "Your app is out of date, please update to the latest version"
                        } else if (isVersionOlder(applicationContext.getInstalledVersion(), config.latestVersion)) {
                            "New update available, please update to the latest version"
                        } else {
                            null
                        }
                        if (msg != null) {
                            makeUpdateNotification(
                                title = AppConstants.Worker.APP_UPDATE_NOTIFICATION_TITLE,
                                message = msg,
                                context = applicationContext
                            )
                        }
                    }

                    adRepository.syncNewAds()

                    Result.success()

                } else {
                    Result.retry()
                }
            } catch (throwable: Throwable) {
                Logger.e(TAG, "Error syncing updates, retrying", throwable)
                Result.retry()
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            // For API 22 and below (deprecated but still functional)
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }


}