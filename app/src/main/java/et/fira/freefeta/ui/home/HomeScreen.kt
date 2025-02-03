package et.fira.freefeta.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import et.fira.freefeta.R
import et.fira.freefeta.ui.AppViewModelProvider
import et.fira.freefeta.ui.navigation.NavigationDestination
import et.fira.freefeta.ui.theme.FreeFetaTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import kotlin.reflect.KFunction2

object HomeDestination: NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@Composable
fun HomeScreen(
    navigateToSettings: () -> Unit,
    navigateToAbout: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    NavDrawer(
        onSettingsClick = navigateToSettings,
        onAboutClick = navigateToAbout,
        isLargeScreen = false
    ) {
        HomeBody(
            downloadItemList = uiState.downloadItemList,
            downloadFile = viewModel::downloadFile,
            modifier = Modifier.fillMaxSize(),
            contentPadding = it
        )
    }
}

@Composable
fun HomeBody(
    downloadItemList: List<DownloadItem>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    downloadFile: (Int, String) -> Unit,
) {
    Surface(
        modifier.padding(contentPadding)
    ) {
        DownloadList(
            listItem = downloadItemList,
            downloadFile= downloadFile,
            modifier = Modifier.fillMaxSize()
        )
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawer(
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    isLargeScreen: Boolean,
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                        .verticalScroll(rememberScrollState())
                ) {
//                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                    NavigationDrawerItem(
                        label = { Text("Home") },
                        icon = { Icon(Icons.Default.Home, null) },
                        selected = true,
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        icon = { Icon(Icons.Default.Settings, null)},
                        selected = false,
                        onClick = {
                            onSettingsClick()
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("About") },
                        icon = { Icon(Icons.Default.Info, null)},
                        selected = false,
                        onClick = {
                            onAboutClick()
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        },
        drawerState = drawerState,
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Free", style = MaterialTheme.typography.displayMedium)
                            Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primaryContainer)
                            Text("Feta", style = MaterialTheme.typography.displayMedium)
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isOpen) {
                                        drawerState.close()
                                    } else {
                                        drawerState.open()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavDrawerPreview() {
    FreeFetaTheme {
        NavDrawer(
            {},
            {},
            false
        ) { }
    }
}