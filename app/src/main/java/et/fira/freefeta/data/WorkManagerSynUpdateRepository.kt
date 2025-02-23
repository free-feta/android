package et.fira.freefeta.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import et.fira.freefeta.util.AppConstants
import et.fira.freefeta.workers.SyncUpdateWorker
import java.util.concurrent.TimeUnit

interface SyncUpdateRepository {
    fun startSyncUpdate()
}

class WorkManagerSynUpdateRepository(context: Context): SyncUpdateRepository {
    private val workManager = WorkManager.getInstance(context)

    override fun startSyncUpdate() {
        val syncUpdateRequest = PeriodicWorkRequestBuilder<SyncUpdateWorker>(2, TimeUnit.HOURS)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        syncUpdateRequest.setConstraints(constraints)

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = AppConstants.Worker.UPDATE_SYNC_WORK_NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = syncUpdateRequest.build()
        )
    }
}