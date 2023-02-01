package runtime.exception.cleanmyandroid.ui.deviceBoost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceBooster

class DeviceBoostViewModel(
    private val deviceBooster: DeviceBooster
) : ViewModel() {

    private var _events = Channel<DeviceBoostEvents>()
    val events = _events.receiveAsFlow()

    val state = deviceBooster.state.map { State(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun startBoosting() {
        deviceBooster.clean()
    }

    fun interruptDeviceBoost() {
        deviceBooster.interrupt()
    }

    fun navigateToSmartCleaning() = viewModelScope.launch {
        _events.send(DeviceBoostEvents.NavigateToSmartCleaning())
    }

    fun navigateToHome() = viewModelScope.launch {
        _events.send(DeviceBoostEvents.NavigateToHome())
    }

    sealed class DeviceBoostEvents {
        class NavigateToHome : DeviceBoostEvents()
        class NavigateToSmartCleaning : DeviceBoostEvents()
    }

    data class State(
        val deviceBoosterState : DeviceBooster.State
    )
}