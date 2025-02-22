package et.fira.freefeta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import et.fira.freefeta.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SplashViewModel(
    application: Application,
): AndroidViewModel(application) {
    private val userPreferencesRepository = (application as FreeFetaApplication).container.userPreferencesRepository

    val uiState: StateFlow<SplashUiState> = userPreferencesRepository.themeMode
        .map { themeMode ->
            SplashUiState(
                isLoading = false,
                theme = themeMode
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SplashUiState(isLoading = true)
        )

}

class SplashViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM
)