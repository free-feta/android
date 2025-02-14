package et.fira.freefeta.ui.ad

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import et.fira.freefeta.R
import et.fira.freefeta.model.Advertisement
import et.fira.freefeta.ui.theme.FreeFetaTheme
import kotlinx.coroutines.delay

@Composable
fun AdDialog(
    ad: Advertisement,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    var countdown by remember { mutableIntStateOf(ad.duration) }
    var isCountdownFinished by remember { mutableStateOf(false) }

    // Countdown effect
    LaunchedEffect(key1 = countdown) {
        if (countdown > 0) {
            delay(1000L) // 1 second delay
            countdown--
        } else {
            isCountdownFinished = true
        }
    }

    Dialog(
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        onDismissRequest = {
            if (isCountdownFinished) {
                onDismiss()
            }
        }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
                .fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(
                                width = 2.dp,
                                shape = RoundedCornerShape(100),
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isCountdownFinished,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(
                                    animationSpec = tween(
                                        500
                                    )
                                )
                            }
                        ) { finished ->
                            if (!finished) {
                                // Display Countdown Text
                                Text(
                                    text = countdown.toString(),
                                    fontSize = 12.sp,
                                    //                            color = Color.Black
                                )
                            } else {
                                // Display Close Button
                                IconButton(
                                    onClick = onDismiss
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = ad.title ?: "Ad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                if (ad.isHtml) {
                    HtmlAd(
                        htmlString = ad.body,
                        Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    TextAd(
                        text = ad.body,
                        url = ad.url,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                AnimatedVisibility(
                    enter = scaleIn(),
                    visible = isCountdownFinished
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onDismiss
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun TextAd(
    text: String,
    url: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Text(
        text = text,
        modifier = modifier.clickable(
            enabled = url != null, onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }
                // Start the activity with the intent
                context.startActivity(intent)
            }
        )
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlAd(
    htmlString: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.setSupportZoom(true)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                htmlString,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun AdDialogPreview() {
    FreeFetaTheme {
        AdDialog(
            Advertisement(
                id = 1,
                isOneTime = false,
                showOnStartup = false,
                isHtml = true,
                title = "This is my Ad",
                body = """
        <html>
            <body>
                <p>Visit <a href="https://t.me/fira_pro">Example</a></p>
                <p>Email <a href="mailto:test@example.com">test@example.com</a></p>
            </body>
        </html>
    """.trimIndent(),
                duration = 3
            ),
            onDismiss = {},

            )
    }
}