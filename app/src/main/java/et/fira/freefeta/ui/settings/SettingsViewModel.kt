package et.fira.freefeta.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import et.fira.freefeta.data.ResizeMode
import et.fira.freefeta.data.SyncUpdateRepository
import et.fira.freefeta.data.ThemeMode
import et.fira.freefeta.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {
    val settingsUiState: StateFlow<SettingsUiState> = userPreferencesRepository.userPreferences
        .map { prefs ->
            SettingsUiState(
                theme = prefs.theme,
                showDeleteConfirmation = prefs.showDeleteConfirmation,
                resizeMode = prefs.resizeMode,
                isFirstLaunch = prefs.isFirstLaunch
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    fun updateTheme(theme: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(theme)
        }
    }

    fun updateDeleteConfirmation(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowDeleteDialog(show)
        }
    }

    fun updateResizeMode(mode: ResizeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setResizeMode(mode)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.completeOnboarding(false)
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            // Implement your update check logic here
        }
    }
}

data class SettingsUiState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val showDeleteConfirmation: Boolean = true,
    val resizeMode: ResizeMode = ResizeMode.FIT,
    val isFirstLaunch: Boolean = true
)


