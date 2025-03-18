package et.fira.freefeta.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.R
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.model.icon
import et.fira.freefeta.ui.AppDestinations
import et.fira.freefeta.ui.FilePermissionHandler
import et.fira.freefeta.ui.ad.AdViewModel
import et.fira.freefeta.ui.navigation.NavigationDestination
import et.fira.freefeta.ui.network.NetworkStatusView
import et.fira.freefeta.ui.player.PlayerDestination
import et.fira.freefeta.ui.theme.FreeFetaTheme
import et.fira.freefeta.util.Util
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction1
import kotlin.reflect.KSuspendFunction0

object HomeDestination: NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.home_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateTo: (String) -> Unit,
    viewModel: HomeViewModel,
    adViewModel: AdViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        ) {
                            Text("Free", style = MaterialTheme.typography.displayMedium)
                            Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primaryContainer)
                            Text("Feta", style = MaterialTheme.typography.displayMedium)
                        }
                        NetworkStatusView(modifier = Modifier.align(Alignment.CenterEnd))
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            FilePermissionHandler()
            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = { neverShow ->
                        viewModel.downloadAction( DownloadAction.ConfirmDelete(neverShow))
                    },
                    onDismiss = {
                        viewModel.downloadAction(DownloadAction.DismissDelete)
                    }

                )
            }
            NavDrawer(
                navigateTo = navigateTo,
            ) {
                if (uiState.downloadItemList.isNotEmpty()) {
                    HomeBody(
                        downloadItemList = uiState.downloadItemList.groupBy {
                            it.file.mediaType?.name ?: it.file.fileType.name
                        },
                        onAction = viewModel::downloadAction,
                        fetchNewFiles = viewModel::fetchNewFiles,
                        navigateTo = navigateTo,
                        triggerAd = adViewModel::triggerAdBeforeAction,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    val coroutineScope = rememberCoroutineScope()
                    val isLoading = remember { mutableStateOf(false) }

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isLoading.value) {
                            CircularProgressIndicator()
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "No entries to show! \nInternet connection is needed to get file list once",
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isLoading.value = true
                                            Toast.makeText(
                                                context,
                                                "Getting files, please wait",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val newFiles = viewModel.fetchNewFiles()
                                            if (newFiles > 0) {
                                                Toast.makeText(
                                                    context,
                                                    "Fetched $newFiles new files",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Getting files failed, please make sure you're connected to internet",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            isLoading.value = false
                                        }
                                    }
                                ) {
                                    Text("Refresh")
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun HomeBody(
    downloadItemList: Map<String, List<DownloadItem>>,
    onAction: (DownloadAction) -> Unit,
    fetchNewFiles: KSuspendFunction0<Int>,
    navigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    triggerAd: KFunction1<() -> Unit, Unit>,
) {
    DownloadList(
        groupedDownloadItemList = downloadItemList,
        onAction = onAction,
        fetchNewFiles = fetchNewFiles,
        navigateTo = navigateTo,
        triggerAd = triggerAd,
        modifier = modifier.fillMaxSize()
    )
}


@Composable
fun NavDrawer(
    navigateTo: (String) -> Unit,
    content: @Composable () -> Unit,
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType = with(adaptiveInfo) {
        when(windowSizeClass.windowWidthSizeClass) {
            androidx.window.core.layout.WindowWidthSizeClass.EXPANDED -> NavigationSuiteType.NavigationDrawer
            androidx.window.core.layout.WindowWidthSizeClass.MEDIUM -> NavigationSuiteType.NavigationRail
            else -> NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        }
    }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(it.icon, null)
                    },
                    label = { Text(stringResource(it.destination.titleRes)) },
                    selected = it == AppDestinations.HOME,
                    onClick = {
                        navigateTo(it.destination.route)
                    }
                )
            }
        },
        layoutType = customNavSuiteType
    ) {
        content()
    }
}

//@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
//@Composable
//fun NavDrawerPreview() {
//    FreeFetaTheme {
//        NavDrawer(
//            {},
//            {},
//        )
//    }
//}

@Preview(showBackground = true)
@Composable
fun DownloadViewPreview() {
    FreeFetaTheme {
        DownloadView(
            DownloadItem(
                file = FileEntity(
                    id = 1,
                    name = "Top Gun: Mavrick smth",
                    downloadUrl = "",
                    size = "1MB",
                    runtime = "1h 34min"
                ),
                downloadModel = if (true) DownloadModel(
                    url = "",
                    path = "",
                    fileName = "",
                    tag = "",
                    id = 1,
                    headers = hashMapOf(),
                    timeQueued = 1,
                    status = Status.PROGRESS,
                    total = 1024*1024,
                    progress = 50,
                    speedInBytePerMs = 1024 * 1024 / 1000f,
                    lastModified = 11,
                    eTag = "",
                    metaData = "",
                    failureReason = ""
                ) else null
            ),
            onAction = {},
            navigateTo = {},
            triggerAd = { action: () -> Unit -> action() } as KFunction1<() -> Unit, Unit>,

//            modifier = Modifier.fillMaxWidth()
        )
    }
}
