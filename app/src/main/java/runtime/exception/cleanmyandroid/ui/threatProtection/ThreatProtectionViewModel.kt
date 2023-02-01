package runtime.exception.cleanmyandroid.ui.threatProtection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceProtector

class ThreatProtectionViewModel(
    val deviceProtector: DeviceProtector
) : ViewModel() {

    private var _events = Channel<ThreatProtectionEvents>()
    val events = _events.receiveAsFlow()

    val state = deviceProtector.state
        .map { State(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun cleanThreats() {
        deviceProtector.clean()
    }

    fun navigateToThreatsList() = viewModelScope.launch {
        _events.send(ThreatProtectionEvents.NavigateToThreatsList())
    }

    fun navigateToBatteryOptimization() = viewModelScope.launch {
        _events.send(ThreatProtectionEvents.NavigateToBatteryOptimization())
    }

    fun interruptProtectDevice() {
        deviceProtector.interrupt()
    }

    sealed class ThreatProtectionEvents {
        class NavigateToThreatsList : ThreatProtectionEvents()
        class NavigateToBatteryOptimization : ThreatProtectionEvents()
    }

    data class State(
        val deviceProtectorState: DeviceProtector.State
    )
}