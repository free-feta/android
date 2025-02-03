package et.fira.freefeta.ui.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import et.fira.freefeta.R
import et.fira.freefeta.ui.navigation.NavigationDestination

object AboutDestination: NavigationDestination {
    override val route = "about"
    override val titleRes = R.string.about_screen_title
}

@Composable
fun AboutScreen(
    navigateBack: () -> Boolean,
    modifier: Modifier = Modifier,
) {}