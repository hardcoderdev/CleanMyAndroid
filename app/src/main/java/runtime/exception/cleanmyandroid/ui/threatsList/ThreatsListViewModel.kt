package runtime.exception.cleanmyandroid.ui.threatsList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceProtector
import runtime.exception.cleanmyandroid.ui.threatsList.models.ItemThreatApp

class ThreatsListViewModel(
    private val deviceProtector: DeviceProtector
) : ViewModel() {

    private var _events = Channel<ThreatsEvents>()
    val events = _events.receiveAsFlow()

    val state = deviceProtector.state.map {
        State(
            if (it is DeviceProtector.State.Cleaned) it.threatApps
            else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), State(emptyList()))

    fun navigateToHome() = viewModelScope.launch {
        _events.send(ThreatsEvents.NavigateToHome())
    }

    sealed class ThreatsEvents {
        class NavigateToHome : ThreatsEvents()
    }

    data class State(val threatApps: List<ItemThreatApp>)
}