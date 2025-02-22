@file:OptIn(ExperimentalMaterial3Api::class)

package et.fira.freefeta.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import et.fira.freefeta.R
import et.fira.freefeta.data.ResizeMode
import et.fira.freefeta.data.ThemeMode
import et.fira.freefeta.ui.AppViewModelProvider
import et.fira.freefeta.ui.navigation.NavigationDestination
import et.fira.freefeta.ui.update.getInstalledVersion
import java.util.Locale

object SettingsDestination: NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.settings_screen_title
}

@Composable
fun SettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.settingsUiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::popBackStack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"

                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = "Appearance") {
                    ThemePreference(
                        selectedTheme = uiState.theme,
                        onThemeSelected = viewModel::updateTheme
                    )
                }
            }

            // Playback Section
            item {
                SettingsSection(title = "Playback") {
                    ResizeModePreference(
                        selectedMode = uiState.resizeMode,
                        onModeSelected = viewModel::updateResizeMode
                    )
                }
            }

            // File Operations Section
            item {
                SettingsSection(title = "File Operations") {
                    DeleteConfirmationPreference(
                        checked = uiState.showDeleteConfirmation,
                        onCheckedChange = viewModel::updateDeleteConfirmation
                    )
                }
            }

            // Application Section
            item {
                SettingsSection(title = "Application") {
                    PreferenceItem(
                        title = "Show Onboarding",
                        subtitle = "View the introduction screens again",
                        onClick = viewModel::resetOnboarding
                    )
                    PreferenceItem(
                        title = "Check for Updates",
                        subtitle = "Current version: ${context.getInstalledVersion()}",
                        onClick = viewModel::checkForUpdate
                    )
                }
            }
        }
    }

}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ThemePreference(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    PreferenceItem(
        title = "App Theme",
        subtitle = "Choose how the app looks",
        onClick = { showDialog = true }
    ) {
        Text(
            text = selectedTheme.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeMode.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = theme == selectedTheme,
                                    onClick = {
                                        onThemeSelected(theme)
                                        showDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = theme == selectedTheme,
                                onClick = {
                                    onThemeSelected(theme)
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = theme.name.lowercase().replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (theme) {
                                        ThemeMode.LIGHT -> "Always use light theme"
                                        ThemeMode.DARK -> "Always use dark theme"
                                        ThemeMode.SYSTEM -> "Follow system theme"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ResizeModePreference(
    selectedMode: ResizeMode,
    onModeSelected: (ResizeMode) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    PreferenceItem(
        title = "Video Resize Mode",
        subtitle = "Choose how videos are displayed",
        onClick = { showDialog = true }
    ) {
        Text(
            text = selectedMode.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Video Resize Mode") },
            text = {
                Column {
                    ResizeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = mode == selectedMode,
                                    onClick = {
                                        onModeSelected(mode)
                                        showDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == selectedMode,
                                onClick = {
                                    onModeSelected(mode)
                                    showDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = mode.name.lowercase().replace('_', ' ').replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (mode) {
                                        ResizeMode.FIT -> "Fit video within screen (black bars)"
                                        ResizeMode.FILL -> "Fill screen (may crop video)"
                                        ResizeMode.ZOOM -> "Zoom to fill (maintains aspect ratio)"
                                        ResizeMode.FIXED_WIDTH -> "Fixed width (adjust height automatically)"
                                        ResizeMode.FIXED_HEIGHT -> "Fixed height (adjust width automatically)"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DeleteConfirmationPreference(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    PreferenceItem(
        title = "Confirm Before Deleting",
        subtitle = "Show a confirmation dialog when deleting files",
        onClick = { onCheckedChange(!checked) }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun PreferenceItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            trailing?.let {
                Spacer(modifier = Modifier.width(16.dp))
                it()
            }
        }
    }
}