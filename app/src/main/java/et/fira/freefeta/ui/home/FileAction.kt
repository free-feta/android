package et.fira.freefeta.ui.home

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.R
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.ui.player.PlayerDestination
import et.fira.freefeta.util.openFile
import kotlin.reflect.KFunction1


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileActionView(
    file: FileEntity,
    downloadModel: DownloadModel?,
    onAction: (DownloadAction) -> Unit,
    navigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    triggerAd: KFunction1<() -> Unit, Unit>,

    ) {
    val context = LocalContext.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {

        if (downloadModel == null  || downloadModel.status == Status.DEFAULT) {
//            Spacer(Modifier.weight(1f))
            ActionIcon(
                iconRes = R.drawable.download_icon,
                contentDescription = stringResource(R.string.download),
                tint = MaterialTheme.colorScheme.primary,
                onClick = { onAction(DownloadAction.Download(context, file)) },
            )
        } else {
            ActionIcon(
                iconRes = R.drawable.delete_icon,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.error,
                onClick = { onAction(DownloadAction.DeleteRequest(downloadModel, file)) }
            )

            val actions = remember(downloadModel.status) {
                getActionsForStatus(downloadModel.status, downloadModel)
            }

            actions.forEach { action ->
                ActionIcon(
                    iconRes = action.iconRes,
                    contentDescription = stringResource(action.contentDescRes),
                    tint = action.tint ?: MaterialTheme.colorScheme.primary,
                    onClick = { onAction(action.onClick) }
                )
            }


            if (downloadModel.status == Status.SUCCESS) {
                ActionIcon(
                    iconRes = R.drawable.folder_icon,
                    contentDescription = stringResource(R.string.open_in_folder),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = {onAction(DownloadAction.OpenFolder(context, file.folderName))}
                )

                ActionIcon(
                    iconRes = R.drawable.external_icon,
                    contentDescription = stringResource(R.string.open_in_external_player),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        triggerAd {
                            onAction(DownloadAction.Open(
                                onOpen = {
                                    context.openFile(downloadModel)
                                }
                            ))
                        }
                    }
                )

                if (file.isPlayable) {
                    ActionIcon(
                        iconRes = R.drawable.play_icon,
                        contentDescription = stringResource(R.string.play),
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            triggerAd {
                                onAction(DownloadAction.Play(
                                    onPlay = {
                                        val encodedFilePath =
                                            Uri.encode("${downloadModel.path}/${downloadModel.fileName}")
                                        navigateTo("${PlayerDestination.route}/$encodedFilePath")
                                    }
                                ))
                            }
                        }
                    )
                }
            }

        }
    }
}


@Composable
fun DeleteConfirmationDialog(
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var neverShowAgain by remember { mutableStateOf(false) }
    Log.d("DeleteDialog", "Recomposing")


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete File") },
        text = {
            Column {
                Text("Are you sure you want to delete this file?")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = neverShowAgain,
                        onCheckedChange = { neverShowAgain = it }
                    )
                    Text(
                        text = "Don't ask again",
                        modifier = Modifier.clickable { neverShowAgain = !neverShowAgain }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(neverShowAgain) }
            ) {
                Text("DELETE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun ActionIcon(
    iconRes: Int,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(28.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}

private fun getActionsForStatus(status: Status, downloadModel: DownloadModel): List<FileAction> {
    return when (status) {
        Status.QUEUED, Status.STARTED, Status.PROGRESS -> listOf(
            FileAction(
                iconRes = R.drawable.cancel_icon,
                contentDescRes = R.string.cancel,
                onClick = DownloadAction.Cancel(downloadModel),
            ),
            FileAction(
                iconRes = R.drawable.pause_icon,
                contentDescRes = R.string.pause,
                onClick = DownloadAction.Pause(downloadModel)
            )
        )
        Status.CANCELLED -> listOf(
            FileAction(
                iconRes = R.drawable.retry_icon,
                contentDescRes = R.string.retry,
                onClick = DownloadAction.Retry(downloadModel)
            )
        )
        Status.FAILED -> listOf(
            FileAction(
                iconRes = R.drawable.cancel_icon,
                contentDescRes = R.string.cancel,
                onClick = DownloadAction.Cancel(downloadModel)
            ),
            FileAction(
                iconRes = R.drawable.retry_icon,
                contentDescRes = R.string.retry,
                onClick = DownloadAction.Retry(downloadModel)
            )
        )
        Status.PAUSED -> listOf(
            FileAction(
                iconRes = R.drawable.resume_icon,
                contentDescRes = R.string.resume,
                onClick = DownloadAction.Resume(downloadModel)
            )
        )
        Status.SUCCESS, Status.DEFAULT -> emptyList()
    }
}

data class FileAction(
    val iconRes: Int,
    val contentDescRes: Int,
    val tint: Color? = null,
    val onClick: DownloadAction
)