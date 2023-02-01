package runtime.exception.cleanmyandroid.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceScanner

class WelcomeViewModel(private val deviceScanner: DeviceScanner) : ViewModel() {

    private var _events = Channel<WelcomeEvents>()
    val events = _events.receiveAsFlow()

    val state = deviceScanner.state.map { State(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        showBanner()
        state.filterNotNull().onEach {
            if (it.deviceScannerState is DeviceScanner.State.Cleaning) {
                if (it.deviceScannerState.currentPercent == 100) {
                    navigateToHome()
                }
            }
        }.launchIn(viewModelScope)
    }

    fun calculateMemorySpace() {
        deviceScanner.clean()
    }

    private fun showBanner() = viewModelScope.launch {
        val adRequest: AdRequest = AdRequest.Builder().build()
        _events.send(WelcomeEvents.ShowBanner(adRequest))
    }

    fun navigateToHome() = viewModelScope.launch {
        _events.send(WelcomeEvents.NavigateToHome())
    }

    data class State(val deviceScannerState: DeviceScanner.State)

    sealed class WelcomeEvents {
        class NavigateToHome : WelcomeEvents()
        data class ShowBanner(val adRequest: AdRequest) : WelcomeEvents()
    }
}