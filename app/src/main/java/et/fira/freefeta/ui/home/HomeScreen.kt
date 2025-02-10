package et.fira.freefeta.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.R
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.model.icon
import et.fira.freefeta.ui.AppDestinations
import et.fira.freefeta.ui.AppViewModelProvider
import et.fira.freefeta.ui.navigation.NavigationDestination
import et.fira.freefeta.ui.theme.FreeFetaTheme

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
    val uiState by viewModel.uiState.collectAsState()
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
                    downloadItemList = uiState.downloadItemList,
                    downloadFile = viewModel::downloadFile,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun HomeBody(
    downloadItemList: List<DownloadItem>,
    modifier: Modifier = Modifier,
    downloadFile: (Int, String) -> Unit,
) {
    DownloadList(
        listItem = downloadItemList,
        downloadFile= downloadFile,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun DownloadList(
    listItem: List<DownloadItem>,
    downloadFile: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(
            items = listItem,
            key = {item -> item.file.id}
        ) {
            DownloadView(
                downloadItem = it,
                downloadFile = downloadFile,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DownloadView(
    downloadItem: DownloadItem,
    downloadFile: (Int, String) -> Unit,
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
    ) {
        Box {
            Column(
                Modifier
                    .padding(8.dp)
                //                .background(Color.Red.copy(alpha = alpha.value))

            ) {
                FileInfoView(
                    file = downloadItem.file,
                    downloadModel = downloadItem.downloadModel,
                    modifier = Modifier
                )
            }

            Badge(stringResource(R.string.badge_new)) // Example count
        }
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
    modifier: Modifier
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
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = file.runtime ?: ""
                )
                Text(
                    text = file.size?.uppercase() ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    fontStyle = FontStyle.Italic
                )
            }
            Spacer(Modifier.height(8.dp))

            FileActionView(
                file = file,
                downloadModel = downloadModel,
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}

@Composable
fun FileActionView(
    file: FileEntity,
    downloadModel: DownloadModel?,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (downloadModel == null) {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(R.drawable.download_icon),
                    contentDescription = stringResource(R.string.download),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_icon),
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            when(downloadModel.status){
                Status.QUEUED, Status.STARTED, Status.PROGRESS -> {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cancel_icon),
                            contentDescription = stringResource(R.string.cancel),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.pause_icon),
                            contentDescription = stringResource(R.string.pause),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Status.SUCCESS -> {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play_icon),
                            contentDescription = stringResource(R.string.play),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Status.CANCELLED, Status.FAILED -> {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.retry_icon),
                            contentDescription = stringResource(R.string.retry),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Status.PAUSED -> {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.resume_icon),
                            contentDescription = stringResource(R.string.resume),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Status.DEFAULT -> TODO()
            }
        }
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
                downloadModel = DownloadModel(
                    url = "",
                    path = "",
                    fileName = "",
                    tag = "",
                    id = 1,
                    headers = hashMapOf(),
                    timeQueued = 1,
                    status = Status.PROGRESS,
                    total = 1000,
                    progress = 60,
                    speedInBytePerMs = 1111f,
                    lastModified = 11,
                    eTag = "",
                    metaData = "",
                    failureReason = ""
                )
            ),
            downloadFile = {i, s -> },
//            modifier = Modifier.fillMaxWidth()
        )
    }
}