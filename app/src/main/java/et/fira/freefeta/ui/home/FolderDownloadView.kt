package et.fira.freefeta.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.model.FileType
import et.fira.freefeta.model.icon
import et.fira.freefeta.ui.ad.AdViewModel
import et.fira.freefeta.ui.search.DownloadItemData
import et.fira.freefeta.ui.theme.FreeFetaTheme
import kotlin.reflect.KFunction1

@Composable
fun FolderDownloadView(
    folderName: String,
    folderItems: List<DownloadItemData>,
    onAction: (DownloadAction) -> Unit,
    navigateTo: (String) -> Unit,
    triggerAd: KFunction1<() -> Unit, Unit>,
    expandedFolder: String,
    modifier: Modifier = Modifier,
    onChangeExpand: () -> Unit,
) {
    val placeholderThumbnail = folderItems.random().file.icon
    val thumbnailUrl: String? = folderItems.filter { downloadItemData ->
        downloadItemData.file.thumbnailUlr != null
    }.randomOrNull()?.file?.thumbnailUlr

    val anyNewItem = folderItems.filter { downloadItemData ->
        downloadItemData.file.isNew
    }

    val isFolderExpanded = expandedFolder == folderName
    val rotationState by animateFloatAsState(
        targetValue = if (isFolderExpanded) 180f else 0f
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                Row(
                    Modifier
                        .padding(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    ThumbnailImage(
                        thumbnailUrl,
                        placeholderThumbnail,
                        { onChangeExpand() }
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(
                        Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = folderName,
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
                                .clickable { onChangeExpand() }
                                .padding(vertical = 8.dp),
                        ) {
                            if (folderItems.isNotEmpty()) {
                                Text(
                                    text = "${folderItems.size} files",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                modifier = Modifier.rotate(rotationState)
                            )
                        }

                    }
                }
                if (anyNewItem.isNotEmpty()) {
                    Badge("New")
                }
            }

        }

        AnimatedVisibility(
            visible = isFolderExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
//                    verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                folderItems.forEach({ downloadItem ->
                    DownloadItem(
                        downloadItem,
                        onAction,
                        navigateTo,
                        triggerAd,
                        showThumbnail = false,
                        colors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                })
            }
        }
    }

}

@Composable
fun FolderItem(downloadItemData: DownloadItemData) {

}

@Preview(showBackground = true)
@Composable
private fun FolderDownloadItemPreview() {
    val adViewModel = AdViewModel(FakeAddRepo());

    FreeFetaTheme {
        FolderDownloadView(
            folderName = "Folder Name",
            folderItems = listOf(
                DownloadItemData(
                    file = FileEntity(
                        id = 1,
                        fileType = FileType.VIDEO,
                        name = "Top Gun: Mavrick smth",
                        downloadUrl = "",
                        size = "1MB",
                        runtime = "1h 34min"

                    ),
                    downloadModel = if (false) DownloadModel(
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
                    ) else null
                )
            ).flatMap { item -> List(10) { item.copy() } },
            onAction = {},
            navigateTo = {},
            triggerAd = adViewModel::triggerAdBeforeAction,
            expandedFolder = "",
            onChangeExpand = {},
        )
    }
}