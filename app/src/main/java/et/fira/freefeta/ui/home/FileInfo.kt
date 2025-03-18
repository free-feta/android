package et.fira.freefeta.ui.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import kotlin.reflect.KFunction1

@Composable
fun FileInfoView(
    file: FileEntity,
    downloadModel: DownloadModel?,
    onAction: (DownloadAction) -> Unit,
    navigateTo: (String) -> Unit,
    modifier: Modifier,
    triggerAd: KFunction1<() -> Unit, Unit>,

    ) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
    ) {
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
                            onAction(DownloadAction.Open(context, downloadModel))
                        }

                    }
                } else {
                    onAction(DownloadAction.Download(context, file))
                }
            }
        )

    }
    Spacer(Modifier.width(12.dp))
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
            Column {
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
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = file.size?.uppercase() ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.weight(1f))

            FileActionView(
                file = file,
                downloadModel = downloadModel,
                onAction = onAction,
                navigateTo = navigateTo,
                triggerAd = triggerAd,
//                    modifier = Modifier.fillMaxWidth()
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
                size = "1MB",
                runtime = "1h 34min",
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
            modifier = Modifier
        )
    }
}