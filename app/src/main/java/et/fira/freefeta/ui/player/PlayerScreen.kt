package et.fira.freefeta.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import et.fira.freefeta.R
import et.fira.freefeta.ui.navigation.NavigationDestination

object PlayerDestination: NavigationDestination {
    override val route = "player"
    override val titleRes = R.string.player_screen_title
}

@Composable
fun PlayerScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Column {
        Text("Player")
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}