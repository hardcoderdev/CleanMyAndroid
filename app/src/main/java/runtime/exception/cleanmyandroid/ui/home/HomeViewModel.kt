package runtime.exception.cleanmyandroid.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceBatteryOptimizer
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceBooster
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceCleaner
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceProtector

class HomeViewModel(
    private val deviceBooster: DeviceBooster,
    private val deviceCleaner: DeviceCleaner,
    private val deviceProtector: DeviceProtector,
    private val deviceBatteryOptimizer: DeviceBatteryOptimizer
) : ViewModel() {

    private var _events = Channel<HomeEvents>()
    val events = _events.receiveAsFlow()
    val state = combine(
        deviceBooster.state,
        deviceCleaner.state,
        deviceProtector.state,
        deviceBatteryOptimizer.state
    ) { boosterState, cleanerState, protectorState, batteryOptimizerState ->
        val features = listOf(
            FeatureItem.Boost(
                isEnabled = boosterState is DeviceBooster.State.Dirty,
                isCompleted = boosterState is DeviceBooster.State.Cleaned
            ),
            FeatureItem.Memory(
                isEnabled = boosterState is DeviceBooster.State.Cleaned,
                isCompleted = cleanerState is DeviceCleaner.State.Cleaned,
                megabytes = when (cleanerState) {
                    is DeviceCleaner.State.Cleaned -> cleanerState.megabytes
                    is DeviceCleaner.State.Dirty -> cleanerState.megabytes
                    else -> 0
                }
            ),
            FeatureItem.Threats(
                isEnabled = cleanerState is DeviceCleaner.State.Cleaned,
                isCompleted = protectorState is DeviceProtector.State.Cleaned,
                appsCount = when (protectorState) {
                    is DeviceProtector.State.Cleaned -> protectorState.installedAppsCount
                    is DeviceProtector.State.Dirty -> protectorState.installedAppsCount
                    else -> 0
                }
            ),
            FeatureItem.Battery(
                isEnabled = protectorState is DeviceProtector.State.Cleaned,
                isCompleted = batteryOptimizerState is DeviceBatteryOptimizer.State.Optimized
            )
        )
        val level = when (features.count { it.isCompleted }) {
            0 -> DeviceProtectionLevel.MINIMUM
            1 -> DeviceProtectionLevel.LOW
            2 -> DeviceProtectionLevel.AVERAGE
            3 -> DeviceProtectionLevel.ABOVE_AVERAGE
            4 -> DeviceProtectionLevel.MAXIMUM
            else -> {
                error("FUCK")
            }
        }

        val progress = when (level) {
            DeviceProtectionLevel.MINIMUM -> 15
            DeviceProtectionLevel.LOW -> 35
            DeviceProtectionLevel.AVERAGE -> 55
            DeviceProtectionLevel.ABOVE_AVERAGE -> 75
            DeviceProtectionLevel.MAXIMUM -> 100
        }

        State(
            featureItems = features,
            deviceProtectionLevel = level,
            deviceProtectionLevelProgress = progress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun navigateToDeviceBoost() = viewModelScope.launch {
        _events.send(HomeEvents.NavigateToDeviceBoost)
    }

    fun navigateToMemoryClean() = viewModelScope.launch {
        _events.send(HomeEvents.NavigateToMemoryClean)
    }

    fun navigateToThreatProtection() = viewModelScope.launch {
        _events.send(HomeEvents.NavigateToThreatProtection)
    }

    fun navigateToBatteryOptimization() = viewModelScope.launch {
        _events.send(HomeEvents.NavigateToBatteryOptimization)
    }

    enum class DeviceProtectionLevel {
        MINIMUM,
        LOW,
        AVERAGE,
        ABOVE_AVERAGE,
        MAXIMUM
    }

    sealed class HomeEvents {
        object NavigateToDeviceBoost : HomeEvents()
        object NavigateToMemoryClean : HomeEvents()
        object NavigateToThreatProtection : HomeEvents()
        object NavigateToBatteryOptimization : HomeEvents()
    }

    data class State(
        val featureItems: List<FeatureItem>,
        val deviceProtectionLevel: DeviceProtectionLevel,
        val deviceProtectionLevelProgress: Int
    )

    sealed class FeatureItem {
        abstract val isEnabled: Boolean
        abstract val isCompleted: Boolean

        data class Boost(override val isEnabled: Boolean, override val isCompleted: Boolean) : FeatureItem()
        data class Memory(override val isEnabled: Boolean, override val isCompleted: Boolean, val megabytes: Int) : FeatureItem()
        data class Threats(override val isEnabled: Boolean, override val isCompleted: Boolean, val appsCount: Int) : FeatureItem()
        data class Battery(override val isEnabled: Boolean, override val isCompleted: Boolean) : FeatureItem()
    }
}