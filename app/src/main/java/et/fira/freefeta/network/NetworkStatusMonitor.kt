package et.fira.freefeta.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.compose.ui.graphics.Color
import et.fira.freefeta.util.AppConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class NetworkStatusMonitor(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.NoInternet)
    val networkState: StateFlow<NetworkState> = _networkState

    // URLs to check (replace with your actual URLs)
    private val zeroRatingUrl = AppConstants.Network.ZERO_RATING_URL
    private val genericInternetUrl = "https://www.google.com"

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkNetworkState(network)
        }

        override fun onLost(network: Network) {
            updateState(NetworkState.NoInternet)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            checkNetworkState(network)
        }
    }

    private fun checkNetworkState(network: Network) {
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

        if (!hasInternet) {
            updateState(NetworkState.NoInternet)
            return
        }

        coroutineScope.launch {
            val canAccessZeroRating = checkConnectivity(zeroRatingUrl, network)
            val canAccessGeneric = checkConnectivity(genericInternetUrl, network)

            _networkState.value = when {
                !canAccessZeroRating && !canAccessGeneric -> NetworkState.ZeroRatingUnreachable
                canAccessZeroRating && !canAccessGeneric -> NetworkState.ZeroRatingOnly
                !canAccessZeroRating && canAccessGeneric -> NetworkState.ZeroRatingUnreachable
                else -> NetworkState.FullInternet
            }
        }
    }

    private suspend fun checkConnectivity(url: String, network: Network): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Bind to the specific network first
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.bindProcessToNetwork(network)
                }
                var connection: HttpURLConnection? = null
                try {
                    connection = URL(url).openConnection() as HttpURLConnection
                    if (url == zeroRatingUrl) {
                        connection.requestMethod = "GET"
                    } else {
                        connection.requestMethod = "HEAD"
                    }
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    if (url == zeroRatingUrl) {
                        for (header in AppConstants.Network.HEADER_FOR_ZERO_RATING_URL) {
                            connection.setRequestProperty(header.key, header.value)
                        }
                    }
                    connection.responseCode == 200

                } finally {
                    connection?.disconnect()
                    // Reset network binding
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        connectivityManager.bindProcessToNetwork(null)
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    fun startMonitoring() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun updateState(newState: NetworkState) {
        coroutineScope.launch {
            _networkState.value = newState
        }
    }
}

sealed class NetworkState {
    data object NoInternet : NetworkState() // Data is off
    data object ZeroRatingUnreachable : NetworkState() // Mobile data on, but zero-rating site unreachable
    data object ZeroRatingOnly : NetworkState() // Zero-rating site accessible, others blocked
    data object FullInternet : NetworkState() // Everything accessible (data plan active)
}

data class NetworkStatusData(
    val color: Color,
    val title: String,
    val description: String
)

fun NetworkState.getStatusData(): NetworkStatusData {
    return when (this) {
        NetworkState.FullInternet -> NetworkStatusData(
            color = Color(0xFF00E2D1),
            title = "Connected",
            description = "You are connected to the internet!\n" +
                    "Note: If you're using airtime or a data package, Ethio Telecom may charge you—unless, of course, you're on Wi-Fi or have unlimited data.\n" +
                    "To be safe, use a SIM card with zero balance and no active data package. 😊"
        )
        NetworkState.NoInternet -> NetworkStatusData(
            color = Color.Red,
            title = "Disconnected",
            description = "No internet connection.\n" +
                    "Please turn on mobile data or connect to Wi-Fi."
        )
        NetworkState.ZeroRatingOnly -> NetworkStatusData(
            color = Color(0xFF00E100),
            title = "Connected to Free Feta",
            description = "You can download files free of charge—zero airtime and no data package required. 💪"
        )
        NetworkState.ZeroRatingUnreachable -> NetworkStatusData(
            color = Color(0xFFFFBD92),
            title = "Free Feta Service Unreachable",
            description = "- Make sure you're connected to the Ethio Telecom 🇪🇹 network and not using a VPN.\n" +
                    "- Try toggling airplane mode ✈️ on and off a few times.\n" +
                    "- Try using a different SIM card.\n" +
                    "If the issue persists, feel free to reach out for support! 🙂"
        )
    }
}