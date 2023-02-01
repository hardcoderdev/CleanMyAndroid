package runtime.exception.cleanmyandroid.ui.smartCleaning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceCleaner
import runtime.exception.cleanmyandroid.common.providers.InstalledAppsProvider

class SmartCleaningViewModel(
    private val deviceCleaner: DeviceCleaner,
    private val installedAppsProvider: InstalledAppsProvider
) : ViewModel() {

    private var _events = Channel<SmartCleaningEvents>()
    val events = _events.receiveAsFlow()

    val state = combine(
        deviceCleaner.state,
        installedAppsProvider.state
    ) { deviceCleanerState, installedAppsState ->
        State(installedAppsState, deviceCleanerState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun interruptSmartCleaning() {
        deviceCleaner.interrupt()
    }

    fun loadInstalledApps() {
        installedAppsProvider.loadInstalledApps()
    }

    fun cleanJunk() {
        deviceCleaner.clean()
    }

    fun navigateToThreatProtection() = viewModelScope.launch {
        _events.send(SmartCleaningEvents.NavigateToThreatProtection())
    }

    fun navigateToHome() = viewModelScope.launch {
        _events.send(SmartCleaningEvents.NavigateToHome())
    }

    sealed class SmartCleaningEvents {
        class NavigateToHome : SmartCleaningEvents()
        class NavigateToThreatProtection() : SmartCleaningEvents()
    }

    data class State(
        val installedAppsState:InstalledAppsProvider.State,
        val deviceCleanerState: DeviceCleaner.State
    )
}