package et.fira.freefeta.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import et.fira.freefeta.R
import et.fira.freefeta.ui.navigation.NavigationDestination
import java.io.File

object PlayerDestination: NavigationDestination {
    override val route = "player"
    override val titleRes = R.string.player_screen_title
}



@UnstableApi
@Composable
fun VideoPlayerScreen(
//    fileUri: Uri,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    // Example for internal storage file:
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "sample-30s.mp4")
    val fileUri = Uri.fromFile(file)

    // Player initialization
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                val dataSourceFactory = DefaultDataSource.Factory(context)
                val mediaItem = MediaItem.fromUri(fileUri)
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = true
                prepare()
            }
    }

    // Track aspect ratio and orientation
    var aspectRatio by remember { mutableStateOf(1f) }
    var originalOrientation by remember { mutableStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }

    // Handle orientation changes
    DisposableEffect(Unit) {
        originalOrientation = activity.requestedOrientation

        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            activity.requestedOrientation = originalOrientation
            exoPlayer.release()
        }
    }

    // Update orientation when aspect ratio changes
    DisposableEffect(aspectRatio) {
        val newOrientation = if (aspectRatio > 1f) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        activity.requestedOrientation = newOrientation

        onDispose {
            // Reset to original orientation when leaving
            activity.requestedOrientation = originalOrientation
        }
    }

    // Player view
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}