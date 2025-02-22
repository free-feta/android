package et.fira.freefeta.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import et.fira.freefeta.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OnboardingScreenViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
    val onboardingCompleted: Flow<Boolean> = userPreferencesRepository.onboardingCompleted

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.completeOnboarding()
        }

    }
}