package runtime.exception.cleanmyandroid.common.providers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BatteryProvider(context: Context) {

    private val mutableState = MutableStateFlow(State(100))
    val state = mutableState.asStateFlow()

    init {
        val batteryIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, batteryIntentFilter)
        val level = checkNotNull(intent).getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        mutableState.update { State(level) }
    }

    data class State(val percentageBatteryLevel: Int)
}