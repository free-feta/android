@file:OptIn(ExperimentalPermissionsApi::class)
package et.fira.freefeta.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import et.fira.freefeta.hasFilePermission

@Composable
fun FilePermissionHandler(
) {
    val context = LocalContext.current

    if (context.hasFilePermission()) {
        return
    }

    // Handle permissions based on API level
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // For API 30+ (Android 11+)
        // Use Manage External Storage permission
        ManageExternalStoragePermission()
    } else {
        // For API 29 and below, request read/write permissions
        val permissionState = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )

        LaunchedEffect(permissionState.allPermissionsGranted) {
            if (permissionState.allPermissionsGranted) {
                onPermissionGranted(context)
            }
        }

        AnimatedVisibility (
            visible = !permissionState.allPermissionsGranted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            )
            {
                Column {
                    Text("Storage permissions are required to download files.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        modifier = Modifier.align(Alignment.End),
                        onClick = {
                            permissionState.launchMultiplePermissionRequest()
                        }
                    ) {
                        Text("Request Permissions")
                    }
                }
            }
        }

    }
}

@Composable
fun ManageExternalStoragePermission(
) {
    val context = LocalContext.current

    // Check if the Manage External Storage permission is granted
    val isPermissionGranted = remember {
        mutableStateOf(hasManageExternalStoragePermission())
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Recheck permission after the user returns from the settings screen
        isPermissionGranted.value = hasManageExternalStoragePermission()
        if (isPermissionGranted.value) {
            onPermissionGranted(context)
        } else {
            onPermissionDenied(context)
        }
    }

    LaunchedEffect(isPermissionGranted.value) {
        if (isPermissionGranted.value) {
            onPermissionGranted(context)
        }
    }

    AnimatedVisibility(
        visible = !isPermissionGranted.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column {
                Text("Manage External Storage permission is required to download files.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        // Launch settings for the user to grant Manage External Storage permission
                        val intent = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                        settingsLauncher.launch(
                            Intent(intent).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    }
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }

}

fun hasManageExternalStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        android.os.Environment.isExternalStorageManager()
    } else {
        true // Permissions are managed differently for API < 30
    }
}

fun onPermissionDenied(context: Context) {
    Toast.makeText(context, "Permission denied! Cannot proceed.", Toast.LENGTH_SHORT).show()
}
fun onPermissionGranted(context: Context) {
    Toast.makeText(context, "Permission granted!", Toast.LENGTH_SHORT).show()
}