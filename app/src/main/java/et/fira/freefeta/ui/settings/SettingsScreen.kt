package et.fira.freefeta.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import et.fira.freefeta.R
import et.fira.freefeta.ui.navigation.NavigationDestination

object SettingsDestination: NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.settings_screen_title
}

@Composable
fun SettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Scaffold {
        Column(Modifier.padding(it)) {
            Text("Settings")
            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
    }

}