package et.fira.freefeta.ui.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.R
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.model.FileType
import et.fira.freefeta.model.icon
import et.fira.freefeta.ui.ad.AdViewModel
import et.fira.freefeta.ui.player.PlayerDestination
import et.fira.freefeta.ui.theme.FreeFetaTheme
import et.fira.freefeta.util.openFile
import kotlin.reflect.KFunction1

@Composable
fun FileInfoView(
    file: FileEntity,
    downloadModel: DownloadModel?,
    onAction: (DownloadAction) -> Unit,
    navigateTo: (String) -> Unit,
    triggerAd: KFunction1<() -> Unit, Unit>,
    showThumbnail: Boolean,
    modifier: Modifier = Modifier,

    ) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
    ) {
        if (showThumbnail) {
            ThumbnailImage(
                url = file.thumbnailUlr,
                placeholder = file.icon,
                onClick = {
                    if (downloadModel != null) {
                        if (downloadModel.status == Status.SUCCESS) {
                            if (file.isPlayable) {
                                triggerAd {
                                    onAction(DownloadAction.Play(
                                        onPlay = {
                                            val encodedFilePath =
                                                Uri.encode("${downloadModel.path}/${downloadModel.fileName}")
                                            navigateTo("${PlayerDestination.route}/$encodedFilePath")
                                        }
                                    ))
                                }
                            } else {
                                triggerAd {
                                    onAction(DownloadAction.Open(
                                        onOpen = {
                                            context.openFile(downloadModel)
                                        }
                                    ))
                                }
                            }

                        }
                    } else {
                        onAction(DownloadAction.Download(context, file))
                    }
                }
            )
        }

        Spacer(Modifier.width(12.dp))
        FileDetailWithAction(file, downloadModel, onAction, navigateTo, triggerAd)
    }

}

@Composable
fun FileDetailWithAction(
    file: FileEntity,
    downloadModel: DownloadModel?,
    onAction: (DownloadAction) -> Unit,
    navigateTo: (String) -> Unit,
    triggerAd: KFunction1<() -> Unit, Unit>
) {
    Column {
        Text(
            text = file.name,
//                style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
//                    .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
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
                                    text = file.runtime,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        Text(
                            text = file.size?.uppercase() ?: "",
                            style = MaterialTheme.typography.labelLarge,
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (downloadModel != null &&
                        downloadModel.status != Status.DEFAULT
                    ) {
//                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .padding(end = 4.dp)
                        ) {
                            Text(
                                text = when (downloadModel.status) {
                                    Status.QUEUED -> stringResource(R.string.queued)
                                    Status.STARTED -> stringResource(R.string.started)
                                    Status.PROGRESS -> stringResource(R.string.downloading)
                                    Status.SUCCESS -> stringResource(R.string.finished)
                                    Status.CANCELLED -> stringResource(R.string.cancelled)
                                    Status.FAILED -> stringResource(
                                        R.string.failed,
                                        downloadModel.failureReason
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
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium,

                                )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            FileActionView(
                file = file,
                downloadModel = downloadModel,
                onAction = onAction,
                navigateTo = navigateTo,
                triggerAd = triggerAd,
                    modifier = Modifier.padding(2.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun FileInfoPreview() {
    val adViewModel = AdViewModel(FakeAddRepo());
    FreeFetaTheme {
        FileInfoView(
            file = FileEntity(
                id = 1,
                fileType = FileType.VIDEO,
                name = "Top Gun: Mavrick smth",
                downloadUrl = "",
                size = "999.8MB",
                runtime = "01:23:45",
                isPlayable = true
            ),
            downloadModel = if (true) DownloadModel(
                url = "",
                path = "",
                fileName = "",
                tag = "",
                id = 1,
                headers = hashMapOf(),
                timeQueued = 1,
                status = Status.SUCCESS,
                total = 1024 * 1024,
                progress = 50,
                speedInBytePerMs = 1024 * 1024 / 1000f,
                lastModified = 11,
                eTag = "",
                metaData = "",
                failureReason = ""
            ) else null,
            onAction = {},
            navigateTo = {},
            triggerAd = adViewModel::triggerAdBeforeAction,
            modifier = Modifier,
            showThumbnail = true
        )
    }
}