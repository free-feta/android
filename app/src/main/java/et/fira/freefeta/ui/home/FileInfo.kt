package et.fira.freefeta.ui.home

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.R
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.model.icon
import et.fira.freefeta.ui.player.PlayerDestination
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
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clickable(
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
        ){
            if (file.thumbnailUlr != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(file.thumbnailUlr)
                        .crossfade(true)
                        .build(),
//                    error = {
//                        Image(
//                            painter = painterResource(file.icon),
//                            contentDescription = null,
//                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
//                            modifier = Modifier.fillMaxSize()
//
//                        )
//                    },
//                    loading = {
//                        Image(
//                            painter = painterResource(R.drawable.loading_img),
//                            contentDescription = null,
//                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
//                            modifier = Modifier.fillMaxSize()
//
//                        )
//                    },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val state by painter.state.collectAsState()
                    if (state is AsyncImagePainter.State.Success) {
                        SubcomposeAsyncImageContent(
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Image(
                            painter = painterResource(file.icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,

                            )
//                        CircularProgressIndicator()
                    }
                }
            } else {
                Image(
                    painter = painterResource(file.icon),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxSize()

                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = file.name,
                style = MaterialTheme.typography.displaySmall,
                fontSize = 20.sp
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
                navigateTo = navigateTo,
                triggerAd = triggerAd,
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}