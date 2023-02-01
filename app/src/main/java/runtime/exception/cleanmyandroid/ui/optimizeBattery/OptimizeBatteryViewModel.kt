package runtime.exception.cleanmyandroid.ui.optimizeBattery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceBatteryOptimizer

class OptimizeBatteryViewModel(private val deviceBatteryOptimizer: DeviceBatteryOptimizer) : ViewModel() {

    private var _events = Channel<OptimizeBatteryEvents>()
    val events = _events.receiveAsFlow()

    val state = deviceBatteryOptimizer.state.map { State(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun navigateToHome() = viewModelScope.launch {
        _events.send(OptimizeBatteryEvents.NavigateToHome())
    }

    fun optimizeBattery(optimizationMode: DeviceBatteryOptimizer.BatteryOptimizationMode) {
        deviceBatteryOptimizer.optimize(optimizationMode)
    }

    fun interruptBatteryOptimize() {
        deviceBatteryOptimizer.interrupt()
    }

    sealed class OptimizeBatteryEvents {
        class NavigateToHome : OptimizeBatteryEvents()
    }

    data class State(val deviceBatteryOptimizerState: DeviceBatteryOptimizer.State)
}