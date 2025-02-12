package et.fira.freefeta.ui.home

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.R
import et.fira.freefeta.util.Util
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.model.icon
import et.fira.freefeta.ui.AppDestinations
import et.fira.freefeta.ui.AppViewModelProvider
import et.fira.freefeta.ui.navigation.NavigationDestination
import et.fira.freefeta.ui.theme.FreeFetaTheme
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction0

object HomeDestination: NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.home_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateTo: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    windowSize: WindowWidthSizeClass,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchNewFilesAndNotify(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Free", style = MaterialTheme.typography.displayMedium)
                        Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primaryContainer)
                        Text("Feta", style = MaterialTheme.typography.displayMedium)
                    }
                }
            )
        }
    ) { contentPadding ->
        Surface(
            modifier = Modifier.padding(contentPadding)
        ) {
            NavDrawer(
                navigateTo = navigateTo,
            ) {
                HomeBody(
                    downloadItemList = uiState.downloadItemList.groupBy {
                        it.file.mediaType?.name ?: it.file.fileType.name
                    },
                    onAction = { downloadAction: DownloadAction, file: FileEntity, downloadModel: DownloadModel?, neverShowAgain: Boolean? ->
                        viewModel.downloadAction(
                            context = context,
                            downloadAction = downloadAction,
                            file = file,
                            downloadModel = downloadModel,
                            neverShowAgain = neverShowAgain
                        )
                               },
                    fetchNewFiles = viewModel::fetchNewFiles,
                    showDeleteDialog = viewModel.showDialog.value,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun HomeBody(
    downloadItemList: Map<String, List<DownloadItem>>,
    onAction: (DownloadAction, FileEntity, DownloadModel?, neverShowAgain: Boolean?) -> Unit,
    fetchNewFiles: KSuspendFunction0<Int>,
    showDeleteDialog: Boolean,
    modifier: Modifier = Modifier,
) {
    DownloadList(
        groupedDownloadItemList = downloadItemList,
        onAction = onAction,
        fetchNewFiles = fetchNewFiles,
        showDeleteDialog = showDeleteDialog,
        modifier = modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DownloadList(
    groupedDownloadItemList: Map<String, List<DownloadItem>>,
    onAction: (DownloadAction, FileEntity, DownloadModel?, neverShowAgain: Boolean?) -> Unit,
    fetchNewFiles: KSuspendFunction0<Int>,
    showDeleteDialog: Boolean,
    modifier: Modifier = Modifier,
) {
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                val newFiles = fetchNewFiles()
                if (newFiles > 0) {
                    Toast.makeText(context, "Fetched $newFiles new files", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No new files released", Toast.LENGTH_SHORT).show()
                }

                isRefreshing = false
            }
        },
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        ) {
            groupedDownloadItemList.forEach { (type, itemList) ->
                stickyHeader {
                    Text(
                        text = type,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp)
                    )

                }

                items(
                    items = itemList,
                    key = {item -> item.file.id}
                ) {
                    DownloadView(
                        downloadItem = it,
                        onAction = onAction,
                        showDeleteDialog = showDeleteDialog,
                        modifier = Modifier.fillMaxWidth().animateItem()
                    )
                }
            }

        }
    }

}

@Composable
fun DownloadView(
    downloadItem: DownloadItem,
    onAction: (DownloadAction, FileEntity, DownloadModel?, neverShowAgain: Boolean?) -> Unit,
    showDeleteDialog: Boolean,
    modifier: Modifier = Modifier,
) {
    val alpha = remember { Animatable(0f) }

    // Animate from transparent to full visibility when recomposed
    LaunchedEffect(downloadItem) {
        alpha.snapTo(1f)
        alpha.animateTo(0f, animationSpec = tween(500))
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
    ) {
        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onConfirm = { neverShow ->
                    onAction(
                        DownloadAction.CONFIRM_DELETE,
                        downloadItem.file,
                        downloadItem.downloadModel,
                        neverShow
                    )
                },
                onDismiss = {onAction(DownloadAction.DISMISS_DELETE, downloadItem.file, downloadItem.downloadModel, null)}

            )
        }
        Column {
            Box(
                Modifier
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 0.dp)
            ) {
                Column(
                    Modifier
                        .padding(end = 8.dp)
                ) {
                    FileInfoView(
                        file = downloadItem.file,
                        downloadModel = downloadItem.downloadModel,
                        onAction = onAction,
                        modifier = Modifier
                    )

                    if (downloadItem.downloadModel != null && downloadItem.downloadModel.status == Status.PROGRESS) {
                        DownloadStatusView(
                            downloadModel = downloadItem.downloadModel,
                            modifier = Modifier
                        )
                    }

                }

                Badge(stringResource(R.string.badge_new))
            }
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 2.dp, bottom = 2.dp)
            ) {
                Text(
                    text = if (downloadItem.downloadModel == null) {
                        ""
                    } else {
                        when(downloadItem.downloadModel.status) {
                            Status.QUEUED -> stringResource(R.string.queued)
                            Status.STARTED -> stringResource(R.string.started)
                            Status.PROGRESS -> stringResource(R.string.downloading)
                            Status.SUCCESS -> stringResource(R.string.finished)
                            Status.CANCELLED -> stringResource(R.string.cancelled)
                            Status.FAILED -> stringResource(
                                R.string.failed,
                                downloadItem.downloadModel.failureReason
                            )
                            Status.PAUSED -> stringResource(R.string.paused)
                            Status.DEFAULT -> ""
                        }
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun DownloadStatusView(downloadModel: DownloadModel, modifier: Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue = downloadModel.progress.div(100f),
        animationSpec = ProgressIndicatorDefaults. ProgressAnimationSpec
    )
    Column {
        Spacer(Modifier.height(8.dp))
//        HorizontalDivider()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text("${
                Util.getTotalLengthText(
                    downloadModel . total * downloadModel . progress / 100
            )
        }/${Util.getTotalLengthText(downloadModel.total)}",
                fontSize = 12.sp)
            Text(
                Util.getCompleteText(
                downloadModel.speedInBytePerMs,
                downloadModel.progress,
                downloadModel.total
            ),
                fontSize = 12.sp)
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 0.dp)
                .height(6.dp)

        )
    }
}

@Composable
fun Badge(text: String) {
    Box(
        modifier = Modifier
//            .size(40.dp)
            .background(MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(bottomEnd = 8.dp,)
            )
        ,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            modifier = Modifier.padding(2.dp)
        )
    }
}


@Composable
fun FileInfoView(
    file: FileEntity,
    downloadModel: DownloadModel?,
    onAction: (DownloadAction, FileEntity, DownloadModel?, neverShowAgain: Boolean?) -> Unit,
    modifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(128.dp)
        ){
            if (file.thumbnailUlr != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(file.thumbnailUlr)
                        .crossfade(true)
                        .build(),
                    error = painterResource(file.icon),
                    placeholder = painterResource(R.drawable.loading_img),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            } else {
                Image(
                    painter = painterResource(file.icon),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.displaySmall
            )
//            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp)
            ) {
                if (file.runtime != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.time_icon),
                            contentDescription = stringResource(R.string.duration),
                            //                    tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = file.runtime
                        )
                    }
                }
                Spacer(Modifier.weight(1f))

                Text(
                    text = file.size?.uppercase() ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(28.dp))

            FileActionView(
                file = file,
                downloadModel = downloadModel,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}

@Composable
fun FileActionView(
    file: FileEntity,
    downloadModel: DownloadModel?,
    onAction: (DownloadAction, FileEntity, DownloadModel?, neverShowAgain: Boolean?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {

        if (downloadModel == null  || downloadModel.status == Status.DEFAULT) {
            Spacer(Modifier.weight(1f))
            ActionIcon(
                iconRes = R.drawable.download_icon,
                contentDescription = stringResource(R.string.download),
                tint = MaterialTheme.colorScheme.primary,
                onClick = { onAction(DownloadAction.DOWNLOAD, file, null, null) },
            )
        } else {
            ActionIcon(
                iconRes = R.drawable.delete_icon,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.error,
                onClick = { onAction(DownloadAction.DELETE_REQUEST, file, downloadModel, null) }
            )

            val actions = remember(downloadModel.status) {
                getActionsForStatus(downloadModel.status)
            }

            actions.forEach { action ->
                ActionIcon(
                    iconRes = action.iconRes,
                    contentDescription = stringResource(action.contentDescRes),
                    tint = action.tint ?: MaterialTheme.colorScheme.primary,
                    onClick = {
                        onAction(action.type, file, downloadModel, null) }
                )
            }


            if (downloadModel.status == Status.SUCCESS) {
                ActionIcon(
                    iconRes = R.drawable.folder_icon,
                    contentDescription = stringResource(R.string.open_in_folder),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = {onAction(DownloadAction.OPEN_FOLDER, file, downloadModel, null)}
                )

                ActionIcon(
                    iconRes = R.drawable.external_icon,
                    contentDescription = stringResource(R.string.open_in_external_player),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = {onAction(DownloadAction.OPEN, file, downloadModel, null)}
                )

                if (file.isPlayable) {
                    ActionIcon(
                        iconRes = R.drawable.play_icon,
                        contentDescription = stringResource(R.string.play),
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = {}
                    )
                }
            }

        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var neverShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete File") },
        text = {
            Column {
                Text("Are you sure you want to delete this file?")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = neverShowAgain,
                        onCheckedChange = { neverShowAgain = it }
                    )
                    Text(
                        text = "Don't ask again",
                        modifier = Modifier.clickable { neverShowAgain = !neverShowAgain }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(neverShowAgain) }
            ) {
                Text("DELETE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun ActionIcon(
    iconRes: Int,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

private fun getActionsForStatus(status: Status): List<FileAction> {
    return when (status) {
        Status.QUEUED, Status.STARTED, Status.PROGRESS -> listOf(
            FileAction(
                iconRes = R.drawable.cancel_icon,
                contentDescRes = R.string.cancel,
                type = DownloadAction.CANCEL
            ),
            FileAction(
                iconRes = R.drawable.pause_icon,
                contentDescRes = R.string.pause,
                type = DownloadAction.PAUSE
            )
        )
        Status.CANCELLED -> listOf(
            FileAction(
                iconRes = R.drawable.retry_icon,
                contentDescRes = R.string.retry,
                type = DownloadAction.RETRY
            )
        )
        Status.FAILED -> listOf(
            FileAction(
                iconRes = R.drawable.cancel_icon,
                contentDescRes = R.string.cancel,
                type = DownloadAction.CANCEL
            ),
            FileAction(
                iconRes = R.drawable.retry_icon,
                contentDescRes = R.string.retry,
                type = DownloadAction.RETRY
            )
        )
        Status.PAUSED -> listOf(
            FileAction(
                iconRes = R.drawable.resume_icon,
                contentDescRes = R.string.resume,
                type = DownloadAction.RESUME
            )
        )
        Status.SUCCESS, Status.DEFAULT -> emptyList()
    }
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
            onAction = {a, i, d, l, ->},
            showDeleteDialog = false,
//            modifier = Modifier.fillMaxWidth()
        )
    }
}

data class FileAction(
    val iconRes: Int,
    val contentDescRes: Int,
    val type: DownloadAction,
    val tint: Color? = null
)