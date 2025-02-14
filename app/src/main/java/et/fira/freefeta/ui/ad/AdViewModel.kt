package et.fira.freefeta.ui.ad

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import et.fira.freefeta.data.ad.AdRepository
import et.fira.freefeta.model.Advertisement
import kotlinx.coroutines.launch

class AdViewModel(
    private val adRepository: AdRepository
): ViewModel() {
    private val _adState = mutableStateOf<Advertisement?>(null)
    val adState: State<Advertisement?> get() = _adState

    private var pendingAction: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            adRepository.syncNewAds()
        }
        showInitialAd()
    }

    private fun showInitialAd() {
        viewModelScope.launch {
            _adState.value = adRepository.getStartUpAd()
        }
    }

    fun triggerAdBeforeAction(action: () -> Unit) {
        viewModelScope.launch {
            val ad = adRepository.getOnDemandAd()
            if (ad != null) {
                pendingAction = action
                _adState.value = ad
            } else {
                action()
            }
        }
    }

    fun onAdDismissed() {
        _adState.value = null
        pendingAction?.invoke()
        pendingAction = null
    }
}