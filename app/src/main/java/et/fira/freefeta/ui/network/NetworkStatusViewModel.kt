package et.fira.freefeta.ui.network

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import et.fira.freefeta.FreeFetaApplication
import et.fira.freefeta.network.NetworkState
import et.fira.freefeta.network.NetworkStatusMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class NetworkStatusViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val networkMonitor = NetworkStatusMonitor(
        context = application,
        coroutineScope = viewModelScope
    )

    private val deviceAnalyticsRepo = (application as FreeFetaApplication).container.deviceAnalyticsRepo

    val networkState: StateFlow<NetworkState> = networkMonitor.networkState

    init {
        networkMonitor.startMonitoring()
        viewModelScope.launch {
            networkState.first { state ->
                state == NetworkState.FullInternet }.let {
                sendAnalytics()
            }
        }
    }

    fun restartMonitoring() {
        networkMonitor.stopMonitoring()
        networkMonitor.startMonitoring()
    }

    private suspend fun sendAnalytics() {
        withContext(Dispatchers.IO) {
            try {
                deviceAnalyticsRepo.sendAnalytics()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        Log.d("NetworkStatusViewM", "view model cleared")
        super.onCleared()
        networkMonitor.stopMonitoring()
    }
}