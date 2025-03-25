package et.fira.freefeta.ui.home

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import et.fira.freefeta.ui.search.DownloadItemData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction1
import kotlin.reflect.KSuspendFunction0

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DownloadList(
    downloadItemDataList: List<DownloadItemData>,
    onAction: (DownloadAction) -> Unit,
    fetchNewFiles: KSuspendFunction0<Int>,
    navigateTo: (String) -> Unit,
    triggerAd: KFunction1<() -> Unit, Unit>,
    hasAnyFilterSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var expandedFolderName by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(500)
                val newFiles = fetchNewFiles()
                if (newFiles > 0) {
                    Toast.makeText(context, "Fetched $newFiles new files", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(context, "No new files released", Toast.LENGTH_SHORT).show()
                }

                isRefreshing = false
            }
        },
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            state = listState
        ) {
            downloadItemDataList
                .groupBy {
                    it.file.mediaType?.name ?: it.file.fileType.name
                }.toSortedMap()
                .forEach { (type, itemList) ->
                    if (!hasAnyFilterSelected) {
                        stickyHeader(
                            key = type
                        ) {
                            Text(
                                text = type,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 16.dp)
                            )

                        }
                    }

                    val itemsWithFolder = itemList.filter {
                        it.file.folderName != null
                    }.groupBy { itemData -> itemData.file.folderName }

                    items(
                        items = itemsWithFolder.entries.toList(),
                        key = { item -> item.key!! }
                    ) {
                        FolderDownloadView(
                            folderName = it.key!!,
                            folderItems = it.value,
                            onAction = onAction,
                            navigateTo = navigateTo,
                            triggerAd = triggerAd,
                            expandedFolder = expandedFolderName,
                            onChangeExpand = {
                                expandedFolderName = if (expandedFolderName == it.key!!) {
                                    ""
                                } else {
                                    it.key!!
                                }
                            }
                        )
                    }

                    val filesWithoutFolder = itemList.filter {
                        it.file.folderName == null
                    }

                    items(
                        items = filesWithoutFolder,
                        key = { item -> item.file.id }
                    ) {
                        DownloadItem(
                            downloadItemData = it,
                            onAction = onAction,
                            navigateTo = navigateTo,
                            triggerAd = triggerAd,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        )
                    }
                }
        }
    }

}
