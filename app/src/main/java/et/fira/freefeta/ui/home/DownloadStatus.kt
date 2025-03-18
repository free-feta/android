package et.fira.freefeta.ui.home

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ketch.DownloadModel
import et.fira.freefeta.util.Util

@Composable
fun DownloadStatusView(
    downloadModel: DownloadModel,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = downloadModel.progress.div(100f),
        animationSpec = ProgressIndicatorDefaults. ProgressAnimationSpec
    )
    Column {
        Spacer(Modifier.height(8.dp))
//        HorizontalDivider()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text("${
                Util.getTotalLengthText(
                    downloadModel . total * downloadModel . progress / 100
                )
            }/${Util.getTotalLengthText(downloadModel.total)}",
                fontSize = 12.sp)
            Text(
                Util.getCompleteText(
                    downloadModel.speedInBytePerMs,
                    downloadModel.progress,
                    downloadModel.total
                ),
                fontSize = 12.sp)
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 0.dp)
                .height(6.dp)

        )
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