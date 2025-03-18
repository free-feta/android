package et.fira.freefeta.ui.home

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ketch.Status
import et.fira.freefeta.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction1
import kotlin.reflect.KSuspendFunction0

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DownloadList(
    groupedDownloadItemList: Map<String, List<DownloadItem>>,
    onAction: (DownloadAction) -> Unit,
    fetchNewFiles: KSuspendFunction0<Int>,
    navigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    triggerAd: KFunction1<() -> Unit, Unit>,
) {
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(500)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        ) {
            groupedDownloadItemList.forEach { (type, itemList) ->
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

                items(
                    items = itemList,
                    key = {item -> item.file.id}
                ) {
                    DownloadView(
                        downloadItem = it,
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

@Composable
fun DownloadView(
    downloadItem: DownloadItem,
    onAction: (DownloadAction) -> Unit,
    navigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    triggerAd: KFunction1<() -> Unit, Unit>,

    ) {
//    val alpha = remember { Animatable(0f) }
//
//    // Animate from transparent to full visibility when recomposed
//    LaunchedEffect(downloadItem) {
////        Log.d("DownloadView", "DownloadItem: $downloadItem")
//        alpha.snapTo(1f)
//        alpha.animateTo(0f, animationSpec = tween(500))
//    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
    ) {
//        if (showDeleteDialog) {
//            DeleteConfirmationDialog(
//                onConfirm = { neverShow ->
//                    onAction( DownloadAction.ConfirmDelete(neverShow))
//                },
//                onDismiss = {
//                    onAction(DownloadAction.DismissDelete)
//                }
//
//            )
//        }
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
                        navigateTo = navigateTo,
                        triggerAd = triggerAd,
                        modifier = Modifier
                    )

                    if (downloadItem.downloadModel != null && downloadItem.downloadModel.status == Status.PROGRESS) {
                        DownloadStatusView(
                            downloadModel = downloadItem.downloadModel,
                            modifier = Modifier
                        )
                    }

                }
                if (downloadItem.file.isNew) {
                    Badge(stringResource(R.string.badge_new))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 2.dp, bottom = 2.dp)
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
                                    .replace("telebirr", "freefeta", true)
                                    .replace("chat", "storage", true)
                                    .replace("superapp", "freefeta", true)
                                    .replace("ethiomobilemoney", "free-bucket", true)
                                    .replace("superapp", "storage", true)
                                    .replace("ethiotelecom", "freefeta", true)
                                    .replace("21006", "443", true)
                                    .replace("196.", "74.", true)
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