package et.fira.freefeta.ui.network

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.RichTooltipColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import et.fira.freefeta.network.NetworkState
import et.fira.freefeta.network.getStatusData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction0

@Composable
fun NetworkStatusView(
    modifier: Modifier = Modifier,
    state: NetworkState = NetworkState.NoInternet,
    restartNetworkStateMonitoring: KFunction0<Unit>
) {
//    RippleDotWithTooltip(
//        state = state,
//        modifier = modifier
//    )
    HeartbeatIconWithTooltip(
        state = state,
        restartNetworkStateMonitoring = restartNetworkStateMonitoring,
        modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartbeatIconWithTooltip(
    state: NetworkState,
    modifier: Modifier = Modifier,
    restartNetworkStateMonitoring: KFunction0<Unit>
) {
    var isAnimationActive by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    // Animate icon size with a shrinking heartbeat effect
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAnimationActive) 0.7f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(state) {
        isAnimationActive = true
    }

    // Stop animation after 3 seconds
    LaunchedEffect(isAnimationActive) {
        if (isAnimationActive) {
            delay(3000)
            isAnimationActive = false
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
//            .clickable {
//                isAnimationActive = true
//            }
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                RichTooltip(
                    title = { Text(state.getStatusData().title, fontWeight = FontWeight.Bold) },
                    action = {
                        TextButton(onClick = { scope.launch { tooltipState.dismiss() } }) {
                            Text("Dismiss")
                        }
                    },
                    colors = RichTooltipColors(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(state.getStatusData().description)
                }
            },
            state = tooltipState,
        ) {
            // Fixed size container to prevent layout shifts
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Network status",
                    modifier = Modifier
                        .size(32.dp * (if (isAnimationActive) scale else 1f))
                        .clickable {
                            isAnimationActive = true
                            scope.launch {
                                tooltipState.show()
//                                restartNetworkStateMonitoring()
                            }
                        },
                    tint = state.getStatusData().color
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RippleDotWithTooltip(
    state: NetworkState,
    modifier: Modifier = Modifier
) {
    var isRippleActive by remember { mutableStateOf(true) }

    val transition = rememberInfiniteTransition()

    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    val innerColor = MaterialTheme.colorScheme.surface

    // Animate ripples when active
    val ripple by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (isRippleActive) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(state) {
        isRippleActive = true
    }

    // Stop ripples after 3 seconds
    LaunchedEffect(isRippleActive) {
        if (isRippleActive) {
            delay(3000)
            isRippleActive = false
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(80.dp)
            .clickable {
                isRippleActive = true
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = size / 2f
            val maxRadius = size.minDimension / 2f

            if (isRippleActive) {
                drawCircle(
                    color = state.getStatusData().color.copy(alpha = 1f - ripple),
                    radius = maxRadius * ripple
                )
            }

            // Center dot
            drawCircle(
                color = innerColor,
                radius = maxRadius * 0.4f
            )
        }


//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    RichTooltip(
                        title = { Text(state.getStatusData().title, fontWeight = FontWeight.Bold) },
                        action = {
                            TextButton(onClick = { scope.launch { tooltipState.dismiss() } }) {
                                Text("Dismiss")
                            }
                        },
                        colors = RichTooltipColors(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.onSecondaryContainer,
                            MaterialTheme.colorScheme.onSecondaryContainer,
                            MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(state.getStatusData().description)
                    }
                },
                state = tooltipState,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Network status",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            isRippleActive = true
                            scope.launch {
                                tooltipState.show()
                            }
                        },
                    tint = state.getStatusData().color
                )
            }
    }
}
