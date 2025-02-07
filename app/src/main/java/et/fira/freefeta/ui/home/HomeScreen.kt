package et.fira.freefeta.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import et.fira.freefeta.R
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

    ) {
        Column(Modifier.background(Color.Red.copy(alpha = alpha.value))) {
            Text(downloadItem.file.name)
            Text((downloadItem.downloadModel?.speedInBytePerMs ?: 0).toString())
            Text(downloadItem.downloadModel?.status?.name ?: "no status")
            Button(
                onClick = { downloadFile(downloadItem.file.id, downloadItem.file.downloadUrl) }
            ) {
                Text("Download")
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

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun NavDrawerPreview() {
    FreeFetaTheme {
        NavDrawer(
            {},
            {},
        )
    }
}