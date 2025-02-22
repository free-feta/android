package et.fira.freefeta.data.analytics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.provider.Settings
import org.json.JSONObject

class DeviceAnalytics(private val context: Context) {

    fun collectDeviceData(): Map<String, Any> {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        return mapOf(
            "device_id" to getDeviceId(),
            "device_model" to Build.MODEL,
            "manufacturer" to Build.MANUFACTURER,
            "os_version" to Build.VERSION.RELEASE,
            "app_version" to (packageInfo.versionName ?: "unknown"),
            "screen_resolution" to getScreenResolution(),
            "network_type" to getNetworkType(context),
            "battery_level" to getBatteryLevel(),
            "storage_available" to getAvailableStorage(),
            "ram_available" to getAvailableRAM(),
            "first_install_time" to packageInfo.firstInstallTime,
            "last_update_time" to packageInfo.lastUpdateTime,
            "last_active_time" to System.currentTimeMillis(), // Updated before sending
            "created_at" to System.currentTimeMillis()
        )
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }

    private fun getScreenResolution(): String {
        val metrics = context.resources.displayMetrics
        return "${metrics.widthPixels}x${metrics.heightPixels}"
    }

    private fun getNetworkType(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For API 23+ (Android 6.0+)
            val network = cm.activeNetwork ?: return "No Connection"
            val capabilities = cm.getNetworkCapabilities(network) ?: return "Unknown"

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
                else -> "Unknown"
            }
        } else {
            // For API 22 and below
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            when (activeNetwork?.type) {
                ConnectivityManager.TYPE_WIFI -> "Wi-Fi"
                ConnectivityManager.TYPE_MOBILE -> "Mobile Data"
                ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
                else -> "Unknown"
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    }

    private fun getAvailableStorage(): Long {
        val stat = android.os.StatFs(context.filesDir.absolutePath)
        return stat.availableBytes / (1024 * 1024) // Convert to MB
    }

    private fun getAvailableRAM(): Long {
        val mi = android.app.ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        activityManager.getMemoryInfo(mi)
        return mi.availMem / (1024 * 1024) // Convert to MB
    }

    fun toJson(): String {
        return JSONObject(collectDeviceData()).toString()
    }
}