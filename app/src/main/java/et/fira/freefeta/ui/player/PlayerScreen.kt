package et.fira.freefeta.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.net.Uri
import android.view.OrientationEventListener
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import et.fira.freefeta.R
import et.fira.freefeta.data.toExoPlayerResizeMode
import et.fira.freefeta.ui.AppViewModelProvider
import et.fira.freefeta.ui.navigation.NavigationDestination
import java.io.File

object PlayerDestination: NavigationDestination {
    override val route = "player"
    const val ARG = "path"
    override val titleRes = R.string.player_screen_title
}



@UnstableApi
@Composable
fun PlayerScreen(
    filePath: String,
    onBackPressed: () -> Unit,
    viewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val activity = context as Activity
    val window = context.window
    val view = LocalView.current
//    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

//    LaunchedEffect(resizeModePreference) {
//        Log.d("PlayerScreen", "resizeModePreference: $resizeModePreference")
//    }

    // Example for internal storage file:
    val file = File(filePath)
    LaunchedEffect(Unit) {
        if (!file.exists()) {
            Toast.makeText(context, "File not found, try deleting and re-downloading", Toast.LENGTH_LONG).show()
            onBackPressed()
        }
    }

    val fileUri = Uri.fromFile(file)

    // Player initialization
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
//                val dataSourceFactory = DefaultDataSource.Factory(context)
                val mediaItem = MediaItem.fromUri(fileUri)
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = true
                prepare()
            }
    }

    // Track aspect ratio and orientation
    var aspectRatio by rememberSaveable { mutableFloatStateOf(1f) }
    var originalOrientation by rememberSaveable { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }
    var isReversedLandscape by rememberSaveable { mutableStateOf(false) }
    // Track if the media is playing
    var isPlaying by remember { mutableStateOf(false) }
    var isVideo by remember { mutableStateOf(false) } // Track if the media is video



    // Handle orientation changes
    DisposableEffect(Unit) {
        originalOrientation = activity.requestedOrientation

        val orientationEventListener = object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                if (aspectRatio > 1f) { // Only handle rotation when in landscape
                    when (orientation) {
                        in 55..125 -> { // Device is in reverse landscape
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                            isReversedLandscape = true
                        }
                        in 235..305 -> { // Device is in normal landscape
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            isReversedLandscape = false
                        }
                    }
                }
            }
        }

        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
            }

            override fun onTracksChanged(tracks: Tracks) {
                // Check if the media contains video tracks
                isVideo = tracks.groups.any { group ->
                    group.type == C.TRACK_TYPE_VIDEO
                }
            }
        }

        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }

        exoPlayer.addListener(listener)

        onDispose {
            orientationEventListener.disable()
            exoPlayer.removeListener(listener)
            activity.requestedOrientation = originalOrientation
            exoPlayer.release()
        }
    }

    // Update initial orientation when aspect ratio changes
    DisposableEffect(aspectRatio) {
        val newOrientation = if (aspectRatio > 1f) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE // Use sensor landscape instead of fixed
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        activity.requestedOrientation = newOrientation

        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }

    // Listen to player state changes
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isPlaying = state == Player.STATE_READY && exoPlayer.isPlaying
            }
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Toggle system bars when playing/paused
    LaunchedEffect(isPlaying, isVideo) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        if (isPlaying && isVideo) {
            // Hide bars and enable transient behavior (swipe to show)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Keep the screen on while playing
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            // Show bars when paused
            insetsController.show(WindowInsetsCompat.Type.systemBars())

            // Allow the screen to sleep when paused
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Reset system bars when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            WindowCompat.getInsetsController(window, view).show(WindowInsetsCompat.Type.systemBars())
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box {
        if (playerState.isLoading) {
            CircularProgressIndicator()
        } else {
            AndroidView(
                factory = { playerContext ->
                    PlayerView(playerContext).apply {
                        player = exoPlayer
                        useController = true
                        resizeMode = playerState.resizeMode.toExoPlayerResizeMode()
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                    }
                },
                modifier = Modifier.fillMaxSize().background(Color.Black)
            )
        }
    }
}