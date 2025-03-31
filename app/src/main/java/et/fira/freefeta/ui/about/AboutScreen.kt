package et.fira.freefeta.ui.about

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.DefaultTintColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.fira.freefeta.R
import et.fira.freefeta.ui.navigation.NavigationDestination
import et.fira.freefeta.ui.theme.FreeFetaTheme
import et.fira.freefeta.ui.update.getInstalledVersion
import et.fira.freefeta.util.AppConstants

object AboutDestination: NavigationDestination {
    override val route = "about"
    override val titleRes = R.string.about_screen_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navigateBack: () -> Boolean) {
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current
    var isTroubleshootExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (isTroubleshootExpanded) 180f else 0f
    )
    val context = LocalContext.current
    val appName = stringResource(id = R.string.app_name)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("About", style = MaterialTheme.typography.headlineLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = { navigateBack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"

                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Info Section
                Text(
                    text = "About $appName",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$appName gives you the freedom to download and enjoy movies, series, " +
                                    "podcasts, music, and documents without consuming mobile data or airtime, " +
                                    "powered by a specialized networking service.",
                            fontSize = 16.sp
                        )
                    }
                }

                // Troubleshoot Section
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isTroubleshootExpanded = !isTroubleshootExpanded }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Troubleshooting Guide",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                modifier = Modifier.rotate(rotationState)
                            )
                        }

                        AnimatedVisibility(
                            visible = isTroubleshootExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("• The service only works on the Ethio Telecom network")
                                Text("• Some video codecs may not be supported by the in-app player. Try using an external player like VLC or MX Player.")
                                Text("• Make sure you are not using a VPN for the service to work.")
                                Text("• If connection fails, try toggling airplane mode on and off a few times")
                                Text("• Check your connectivity status and follow the instructions by clicking the info icon (ℹ\uFE0F) at the top right of the Home screen.")
                                Text("• Make sure you have enough storage space for downloads")
                                Text("• If you still encounter issues, feel free to reach out to us. ")
                            }
                        }
                    }
                }

                // Contact Section
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Contact Us",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Telegram Link
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    uriHandler.openUri(AppConstants.About.APP_TG_CHANNEL)
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.telegram_icon),
                                contentDescription = "Telegram",
                                modifier = Modifier.size(28.dp),
                                tint = Color.Unspecified
                            )
                            Text("Join our Telegram Channel")
                        }

                        //YT
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    uriHandler.openUri(AppConstants.About.APP_YT)
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.yt_icon),
                                contentDescription = "Youtube",
                                modifier = Modifier.size(28.dp),
                                tint = Color.Unspecified
                            )
                            Text("Subscribe to our Youtube channel")
                        }

                        //Tiktok
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    uriHandler.openUri(AppConstants.About.APP_TIKTOK)
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.tiktok_icon),
                                contentDescription = "Tiktok",
                                modifier = Modifier.size(28.dp),
                                tint = Color.Unspecified
                            )
                            Text("Follow us on Tiktok")
                        }

                        // Developer Contact
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    uriHandler.openUri("mailto:${AppConstants.About.DEVELOPER_EMAIL}")
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                            )
                            Text(AppConstants.About.DEVELOPER_EMAIL)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    uriHandler.openUri(AppConstants.About.DEVELOPER_TG_ACC)
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Developer Telegram"
                            )
                            Text("Contact developer on Telegram")
                        }
                    }
                }

                // Copyright Disclaimer
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Copyright Disclaimer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "$appName does not store any media files on our servers. All content is provided by third-party services. We only provide links to media content that is hosted and stored by third parties.",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                // App Version
                Text(
                    text = "Version ${try {
                        context.getInstalledVersion()
                    } catch(e: Exception) {
                        "1.0.0"
                    }}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    FreeFetaTheme {
        AboutScreen {true}
    }
}