package et.fira.freefeta.ui.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import et.fira.freefeta.R
import et.fira.freefeta.model.FileType
import et.fira.freefeta.model.MediaType
import et.fira.freefeta.network.NetworkState
import et.fira.freefeta.ui.AppDestinations
import et.fira.freefeta.ui.FilePermissionHandler
import et.fira.freefeta.ui.ad.AdViewModel
import et.fira.freefeta.ui.navigation.NavigationDestination
import et.fira.freefeta.ui.search.CategoryHeader
import et.fira.freefeta.ui.search.DownloadItemData
import et.fira.freefeta.ui.search.FilterChipsRow
import et.fira.freefeta.ui.search.SearchUiState
import et.fira.freefeta.ui.search.SearchViewModel
import et.fira.freefeta.ui.search.TopBarWithSearch
import et.fira.freefeta.ui.theme.FreeFetaTheme
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1
import kotlin.reflect.KSuspendFunction0

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.home_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateTo: (String) -> Unit,
    viewModel: HomeViewModel,
    adViewModel: AdViewModel,
    networkState: NetworkState,
    restartNetworkStateMonitoring: KFunction0<Unit>,
    searchViewModel: SearchViewModel
) {
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val selectedFileType by searchViewModel.selectedFileType.collectAsState()
    val selectedMediaType by searchViewModel.selectedMediaType.collectAsState()
    val availableFileTypes by searchViewModel.availableFileTypes.collectAsState()
    val availableMediaTypes by searchViewModel.availableMediaTypes.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    val isFilterActive by searchViewModel.isFilterActive.collectAsState()

    Surface(
//        topBar = {
//            TopBarWithSearch(
//                networkState = networkState,
//                onSearchQueryChanged = { query ->
//                    searchViewModel.setSearchQuery(query)
//                },
//                isSearchActive = isSearchActive,
//                onSearchActiveChanged = {isSearchActive = it},
//                isFilterActive = isFilterActive,
//                onFilterActiveChanged = searchViewModel::setFilterState,
//                restartNetworkStateMonitoring = restartNetworkStateMonitoring,
//                hasAnyFilterSelected = selectedFileType != null || selectedMediaType != null,
//                searchQuery = searchQuery
//            )
//        }
    ) {
        Column(
            modifier = Modifier.statusBarsPadding()
        ) {
            TopBarWithSearch(
                networkState = networkState,
                onSearchQueryChanged = { query ->
                    searchViewModel.setSearchQuery(query)
                },
                isSearchActive = isSearchActive,
                onSearchActiveChanged = { isSearchActive = it },
                isFilterActive = isFilterActive,
                onFilterActiveChanged = searchViewModel::setFilterState,
                restartNetworkStateMonitoring = restartNetworkStateMonitoring,
                hasAnyFilterSelected = selectedFileType != null || selectedMediaType != null,
                searchQuery = searchQuery
            )
            // Show filter chips when filter is active
            AnimatedVisibility(visible = isFilterActive) {
                Column {
                    // File Type Filters
                    CategoryHeader("File Types")
                    FilterChipsRow(
                        categories = availableFileTypes.map { it.name },
                        selectedCategory = selectedFileType?.name,
                        onCategorySelected = { categoryName ->
                            val fileType = FileType.valueOf(categoryName)
                            searchViewModel.toggleFileTypeFilter(fileType)
                        }
                    )

                    // Media Type Filters (if available)
                    if (availableMediaTypes.isNotEmpty()) {
                        CategoryHeader("Media Types")
                        FilterChipsRow(
                            categories = availableMediaTypes.map { it.name },
                            selectedCategory = selectedMediaType?.name,
                            onCategorySelected = { categoryName ->
                                val mediaType = MediaType.valueOf(categoryName)
                                searchViewModel.toggleMediaTypeFilter(mediaType)
                            }
                        )
                    }

                    // Clear filters button
                    if (selectedFileType != null || selectedMediaType != null) {
                        TextButton(
                            onClick = { searchViewModel.clearFilters() },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 16.dp)
                        ) {
                            Text("Clear Filters")
                        }
                    }

                    HorizontalDivider()
                }
            }
            FilePermissionHandler()
            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = { neverShow ->
                        viewModel.downloadAction(DownloadAction.ConfirmDelete(neverShow))
                    },
                    onDismiss = {
                        viewModel.downloadAction(DownloadAction.DismissDelete)
                    }

                )
            }
            NavDrawer(
                navigateTo = navigateTo,
            ) {
                when (searchUiState) {
                    is SearchUiState.Success -> {
                        if ((searchUiState as SearchUiState.Success).downloadItemDataList.isEmpty()) {
                            if (searchQuery.text.isNotEmpty() || selectedFileType != null && selectedMediaType != null) {
                                Text(
                                    text = if (searchQuery.text.isNotEmpty()) {
                                        if (selectedFileType != null && selectedMediaType != null) {
                                            "No results found for \"${searchQuery.text}\" with ${selectedFileType?.name} and ${selectedMediaType?.name} type"
                                        } else if (selectedFileType != null) {
                                            "No results found for \"${searchQuery.text}\" with ${selectedFileType?.name} type"
                                        } else if (selectedMediaType != null) {
                                            "No results found for \"${searchQuery.text}\" with ${selectedMediaType?.name} type"
                                        } else {
                                            "No results found for \"${searchQuery.text}\""
                                        }
                                    } else {
                                        "No results found with ${selectedFileType?.name} and ${selectedMediaType?.name} type"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

                        } else {
                            DownloadList(
                                downloadItemDataList = (searchUiState as SearchUiState.Success).downloadItemDataList,
                                onAction = viewModel::downloadAction,
                                fetchNewFiles = viewModel::fetchNewFiles,
                                navigateTo = navigateTo,
                                triggerAd = adViewModel::triggerAdBeforeAction,
                                hasAnyFilterSelected = selectedFileType != null || selectedMediaType != null,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    is SearchUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                }

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
        when (windowSizeClass.windowWidthSizeClass) {
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
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
    }
}
