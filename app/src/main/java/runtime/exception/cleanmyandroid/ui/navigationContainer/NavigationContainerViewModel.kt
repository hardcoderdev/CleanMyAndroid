package runtime.exception.cleanmyandroid.ui.navigationContainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.*
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceBooster
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceCleaner
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceProtector

@OptIn(InternalCoroutinesApi::class)
class NavigationContainerViewModel @OptIn(InternalCoroutinesApi::class) constructor(
    deviceBooster: DeviceBooster,
    deviceCleaner: DeviceCleaner,
    deviceProtector: DeviceProtector
) : ViewModel() {

    private val mutableState = MutableStateFlow(initialState)
    val state = mutableState.asStateFlow()

    init {
        combine(
            deviceBooster.state,
            deviceCleaner.state,
            deviceProtector.state
        ) { deviceBoosterState, deviceCleanerState, deviceProtectorState ->
            mutableState.update {
                State(
                    isSmartCleaningEnabled = deviceBoosterState is DeviceBooster.State.Cleaned,
                    isThreatProtectionEnabled = deviceCleanerState is DeviceCleaner.State.Cleaned,
                    isBatteryOptimizedEnabled = deviceProtectorState is DeviceProtector.State.Cleaned
                )
            }
        }.launchIn(viewModelScope)
    }

    data class State(
        val isSmartCleaningEnabled: Boolean,
        val isThreatProtectionEnabled: Boolean,
        val isBatteryOptimizedEnabled: Boolean
    )

    companion object {
        private val initialState = State(
            isSmartCleaningEnabled = true,
            isBatteryOptimizedEnabled = true,
            isThreatProtectionEnabled = true
        )
    }
}