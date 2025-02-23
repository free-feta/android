package et.fira.freefeta.ui.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.fira.freefeta.R
import et.fira.freefeta.ui.theme.FreeFetaTheme
import kotlinx.coroutines.launch

// Data class for onboarding items
data class OnboardingItem(
    val image: Int? = null,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onBoardingComplete: () -> Unit
) {
    // List of onboarding items
    val items = listOf(
        OnboardingItem(
            image = R.drawable.ic_launcher_foreground,
            title = "Welcome to Free Feta",
            description = "Your gateway to entertainment freedom, completely free."
        ),
        OnboardingItem(
//            image = R.drawable.file_image,
            title = "No Data? No Problem!",
            description = "Download videos and files without an active data package or airtime balance."
        ),
        OnboardingItem(
//            image = R.drawable.file_image,
            title = "Easy to use",
            description = "Follow simple steps to connect and start downloading in seconds."
        ),
        OnboardingItem(
//            image = R.drawable.file_image,
            title = "All-in-One Media Player",
            description = "Watch movies and videos, listen to music and podcasts, and explore more—all in one app."
        ),
        OnboardingItem(
//            image = R.drawable.file_image,
            title = "Stay Updated!",
            description = "New content is added regularly. Ensure you have an active internet connection to fetch the latest files."
        )
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { items.size }
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFD700), Color(0xFFFFEA69)), // Gradient colors
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f) // Adjust direction
            )
        )

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)

        ) {
            // Top Skip button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (pagerState.currentPage != items.size - 1) {
                    TextButton(
                        onClick = onBoardingComplete,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = Color(0xFFFFEFA7)
                        )
                    ) {
                        Text(
                            text = "Skip",
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                OnboardingPage(items[page])
            }

            // Bottom section with indicators and buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Page indicators
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(items.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration)
                            Color.DarkGray
                        else
                            Color.DarkGray.copy(alpha = 0.3f)

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Navigation buttons
                Box(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    if (pagerState.currentPage != items.size - 1) {
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        page = pagerState.currentPage + 1,
                                        animationSpec = tween(
                                            durationMillis = 800,  // Customize duration
                                            easing = FastOutSlowInEasing // Customize easing curve
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors().copy(
                                containerColor = Color.DarkGray
                            )
                        ) {
                            Text("Next", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = onBoardingComplete,
                            colors = ButtonDefaults.buttonColors().copy(
                                containerColor = Color.Black
                            )

                        ) {
                            Text("Let's rock!", color = Color.White)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun OnboardingPage(item: OnboardingItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (item.image != null) {
            Image(
                painter = painterResource(id = item.image),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )
        }

        Text(
            text = item.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = item.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.DarkGray.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    FreeFetaTheme {
        OnboardingScreen { }
    }
}