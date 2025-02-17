package et.fira.freefeta.ui.update

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ketch.Status
import et.fira.freefeta.MainActivity
import et.fira.freefeta.R
import et.fira.freefeta.ui.AppViewModelProvider
import et.fira.freefeta.ui.home.DownloadStatusView
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppUpdateDialog(
    modifier: Modifier = Modifier,
    viewModel: UpdateViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val updateUiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val currentVersion = context.getInstalledVersion()

    LaunchedEffect(Unit) {
        viewModel.checkForUpdate(context)
    }
    if (!updateUiState.showUpdateDialog) return

    updateUiState.run {
        if (config != null) {
            Dialog(
                onDismissRequest = {
                    if (!updateUiState.forceUpdate && config.isServiceOk) {
                        viewModel.dismissDialog()
                    }
                },
            ) {
              Card {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(16.dp)
//                            .animateContentSize()
                    ) {
                        if (!config.isServiceOk) {
                            Text(
                                config.errorMessage ?: "Service not available",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { (context as? MainActivity)?.finish() }
                            ) {
                                Text("Exit")
                            }
                            return@Column
                        }
                        if (forceUpdate) {
                            Text(
                                "Mandatory app update required!",
                                style = MaterialTheme.typography.titleLarge
                            )
                        } else {
                            Text("App update available!", style = MaterialTheme.typography.titleLarge)
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Current version: $currentVersion")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Latest version: ${config.latestVersion}")
                            Spacer(modifier = Modifier.height(8.dp))
                            if (config.versionDescription != null) {
                                Text(config.versionDescription)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (downloadModel != null) {
                            if (downloadModel.status == Status.SUCCESS) {
                                Text("Ready to install")
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.installApk(
                                            context,
                                            "${downloadModel.path}/${downloadModel.fileName}"
                                        )
                                    }
                                ) {
                                    Text("Install")
                                }
                            } else {
                                DownloadStatusView(downloadModel = downloadModel)
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.cancelDownload() }
                                    ) {
                                        Text(stringResource(R.string.cancel))
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.retryDownload() }
                                    ) {
                                        Text(stringResource(R.string.retry))
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.downloadUpdate(context, config.downloadUrl) }
                            ) {
                                Text(stringResource(R.string.download))
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            if (!forceUpdate) {
                                OutlinedButton(
                                    onClick = viewModel::dismissDialog
                                ) {
                                    Text("Remind me later")
                                }
                            }

                            if (config.alternativeUrl != null) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.openUpdateLink(
                                            context,
                                            config.alternativeUrl
                                        )
                                    }
                                ) {
                                    Text("External Download")
                                }
                            }

                        }
                    }
                }
            }

        } else viewModel.dismissDialog()



    }

}

@Composable
fun SmartLinkOpener(url: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun handleLinkOpening() {
        if (!isValidHttpUrl(url)) {
            scope.launch {
                snackbarHostState.showSnackbar("Invalid URL format")
            }
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val chooser = Intent.createChooser(intent, "Open link with...")

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("No apps available to open this link")
                }
            }
        } catch (e: ActivityNotFoundException) {
            scope.launch {
                snackbarHostState.showSnackbar("Error opening link")
            }
        }
    }

    // Usage example with icon and text
    Row(
        modifier = Modifier
            .clickable { handleLinkOpening() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Open link"
        )
        Spacer(Modifier.width(8.dp))
        Text("Open External Link")
    }

    SnackbarHost(hostState = snackbarHostState)
}

fun isValidHttpUrl(url: String): Boolean {
    val pattern = Patterns.WEB_URL
    return url.isNotEmpty() && pattern.matcher(url).matches()
}