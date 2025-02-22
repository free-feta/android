package et.fira.freefeta.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import et.fira.freefeta.data.ResizeMode
import et.fira.freefeta.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PlayerViewModel(
    userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
    val playerState: StateFlow<PlayerState> = userPreferencesRepository.resizeMode
        .map { resizeMode ->
            PlayerState(
                resizeMode = resizeMode,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerState(isLoading = true)
        )
}

data class PlayerState(
    val resizeMode: ResizeMode = ResizeMode.FIT,
    val isLoading: Boolean = true
)