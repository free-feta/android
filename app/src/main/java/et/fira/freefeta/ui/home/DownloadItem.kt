package et.fira.freefeta.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ketch.DownloadModel
import com.ketch.Status
import et.fira.freefeta.R
import et.fira.freefeta.data.ad.AdRepository
import et.fira.freefeta.model.Advertisement
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.model.FileType
import et.fira.freefeta.ui.ad.AdViewModel
import et.fira.freefeta.ui.search.DownloadItemData
import et.fira.freefeta.ui.theme.FreeFetaTheme
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KFunction1

@Composable
fun DownloadItem(
    downloadItemData: DownloadItemData,
    onAction: (DownloadAction) -> Unit,
    navigateTo: (String) -> Unit,
    triggerAd: KFunction1<() -> Unit, Unit>,
    modifier: Modifier = Modifier,
    showThumbnail: Boolean = true,
    colors: CardColors = CardDefaults.cardColors()
) {
//    val alpha = remember { Animatable(0f) }
//
//    // Animate from transparent to full visibility when recomposed
//    LaunchedEffect(downloadItemData) {
////        Log.d("DownloadView", "DownloadItem: $downloadItemData")
//        alpha.snapTo(1f)
//        alpha.animateTo(0f, animationSpec = tween(500))
//    }
    Card(
        colors = colors,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
    ) {
        Box(
            contentAlignment = Alignment.TopEnd
        ){
            Column(
                Modifier
                    .padding(8.dp)
            ) {
                Column(
                    Modifier
                        .padding(end = 8.dp)
                ) {
                    FileInfoView(
                        file = downloadItemData.file,
                        downloadModel = downloadItemData.downloadModel,
                        onAction = onAction,
                        navigateTo = navigateTo,
                        triggerAd = triggerAd,
                        showThumbnail = showThumbnail,
                    )

                    if (downloadItemData.downloadModel != null && downloadItemData.downloadModel.status == Status.PROGRESS) {
                        DownloadStatusView(
                            downloadModel = downloadItemData.downloadModel,
                            modifier = Modifier
                        )
                    }

                }

            }
            if (downloadItemData.file.isNew) {
                Badge(stringResource(R.string.badge_new))
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
                shape = RoundedCornerShape(topEnd = 8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 2.dp, end = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DownloadItemPreview() {
    val adViewModel = AdViewModel(FakeAddRepo());
    FreeFetaTheme {
        DownloadItem(
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
            ),
            onAction = {},
            navigateTo = {},
            triggerAd = adViewModel::triggerAdBeforeAction,

//            modifier = Modifier.fillMaxWidth()
        )
    }
}

class FakeAddRepo : AdRepository {
    override suspend fun insertAd(advertisement: Advertisement) {
        TODO("Not yet implemented")
    }

    override suspend fun insertAd(advertisements: List<Advertisement>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAd(advertisement: Advertisement) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAd(advertisements: List<Advertisement>) {
        TODO("Not yet implemented")
    }

    override fun getAd(id: Int): Flow<Advertisement> {
        TODO("Not yet implemented")
    }

    override fun getAllAdsStream(): Flow<List<Advertisement>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchRemoteAds(): List<Advertisement> {
        TODO("Not yet implemented")
    }

    override suspend fun syncNewAds(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getStartUpAd(): Advertisement? {
        TODO("Not yet implemented")
    }

    override suspend fun getOnDemandAd(): Advertisement? {
        TODO("Not yet implemented")
    }

}