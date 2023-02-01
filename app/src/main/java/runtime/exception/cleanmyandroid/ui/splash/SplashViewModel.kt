package runtime.exception.cleanmyandroid.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private var _events = Channel<SplashEvents>()
    val events = _events.receiveAsFlow()

    private var _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> get() = _progress

    fun loadProgressBar() = viewModelScope.launch {
        (1..100).forEach {
            _progress.emit(it)
            delay(SHORT_DELAY)
            if (it == MAX_PROGRESS) showAppStartAd()
        }
    }

    fun navigateToWelcome() = viewModelScope.launch {
        _events.send(SplashEvents.NavigateToWelcome)
    }

    private fun showAppStartAd() = viewModelScope.launch {
        _events.send(SplashEvents.ShowOnOpenAppAd)
    }

    sealed class SplashEvents {
        object ShowOnOpenAppAd : SplashEvents()
        object NavigateToWelcome : SplashEvents()
    }

    companion object {
        private const val MAX_PROGRESS = 100
        private const val SHORT_DELAY = 50L
    }
}