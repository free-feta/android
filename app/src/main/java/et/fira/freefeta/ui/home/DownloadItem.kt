package et.fira.freefeta.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ketch.Status
import et.fira.freefeta.R
import kotlin.reflect.KFunction1

@Composable
fun DownloadItem(
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

@Composable
fun Badge(text: String) {
    Box(
        modifier = Modifier
//            .size(40.dp)
            .background(
                MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(bottomEnd = 8.dp)
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