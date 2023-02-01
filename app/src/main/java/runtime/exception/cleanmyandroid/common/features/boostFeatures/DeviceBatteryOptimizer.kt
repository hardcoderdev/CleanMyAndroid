package runtime.exception.cleanmyandroid.common.features.boostFeatures

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import runtime.exception.cleanmyandroid.common.Logger
import runtime.exception.cleanmyandroid.common.managers.BrightnessManager
import runtime.exception.cleanmyandroid.common.providers.BatteryProvider
import java.lang.Exception
import kotlin.random.Random

// ultra * 2, extreme * 3
class DeviceBatteryOptimizer(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val brightnessManager: BrightnessManager,
    private val batteryProvider: BatteryProvider
) {

    private var cleanJob: Job? = null
    private val mutableState = MutableStateFlow<State>(State.ReadingData())
    val state = mutableState.asStateFlow()

    init {
        load()
    }

    fun optimize(batteryOptimizationMode: BatteryOptimizationMode) {
        if (state.value is State.Optimizing) return
        cleanJob = coroutineScope.launch {
            try {
                progressFlow().collect { percent ->
                    when {
                        percent <= 25 -> {
                            mutableState.update { State.Optimizing(percent, OperationType.OPTIMIZING_BATTERY_USAGE) }
                        }
                        percent <= 50 -> {
                            mutableState.update { State.Optimizing(percent, OperationType.CHECKING_CPU) }
                        }
                        percent <= 75 -> {
                            mutableState.update { State.Optimizing(percent, OperationType.BOOST_BATTERY) }
                        }
                        percent <= 100 -> {
                            mutableState.update { State.Optimizing(percent, OperationType.BOOST_BATTERY) }
                        }
                    }
                }
                when (batteryOptimizationMode) {
                    BatteryOptimizationMode.NORMAL_MODE -> applyNormalMode()
                    BatteryOptimizationMode.ULTRA_MODE -> applyUltraMode()
                    BatteryOptimizationMode.EXTREME_MODE -> applyExtremeMode()
                }

                mutableState.update {
                    val optimizedLevel = when (batteryOptimizationMode) {
                        BatteryOptimizationMode.NORMAL_MODE -> 2
                        BatteryOptimizationMode.ULTRA_MODE -> 3
                        BatteryOptimizationMode.EXTREME_MODE -> 6
                    }

                    val currentLevel = when (batteryOptimizationMode) {
                        BatteryOptimizationMode.NORMAL_MODE -> 1
                        BatteryOptimizationMode.ULTRA_MODE -> 2
                        BatteryOptimizationMode.EXTREME_MODE -> 3
                    }

                    State.Optimized(
                        currentModeTime = Pair(
                            currentLevel * batteryProvider.state.value.percentageBatteryLevel / ONE_HOUR,
                            (currentLevel * batteryProvider.state.value.percentageBatteryLevel / ONE_HOUR) + Random.nextInt(0, 40)
                        ),
                        optimizedModeTime = Pair(
                            optimizedLevel * batteryProvider.state.value.percentageBatteryLevel / ONE_HOUR,
                            (optimizedLevel * batteryProvider.state.value.percentageBatteryLevel / ONE_HOUR) + Random.nextInt(0, 40)
                        ),
                        batteryInfo = batteryProvider.state.value,
                        batteryOptimizationMode = batteryOptimizationMode
                    )
                }

            } catch (e: CancellationException) {
                load()
            }
            cleanJob = null
        }
    }

    private fun progressFlow() = flow {
        repeat(101) {
            emit(it)
            if (it % 25 == 0 && it != 100) {
                delay(LONG_DELAY)
            } else {
                delay(SHORT_DELAY)
            }
        }
    }

    fun interrupt() {
        cleanJob?.cancel()
        cleanJob = null
    }

    private fun load() {
        mutableState.update {
            State.NotOptimized(
                currentModeTime = Pair(
                    3 * batteryProvider.state.value.percentageBatteryLevel / ONE_HOUR,
                    (3 * batteryProvider.state.value.percentageBatteryLevel / ONE_HOUR) + Random.nextInt(0, 10)
                ),
                optimizedModeTime = Pair(
                    6 * batteryProvider.state.value.percentageBatteryLevel / ONE_HOUR,
                    (6 * batteryProvider.state.value.percentageBatteryLevel / ONE_HOUR) + Random.nextInt(0, 10)
                ),
                batteryInfo = batteryProvider.state.value
            )
        }
    }

    private fun applyNormalMode() = coroutineScope.launch {
        disableBluetooth()
    }

    private fun applyUltraMode() = coroutineScope.launch {
        disableBluetooth()
        brightnessManager.changeBrightness(55)
    }

    private fun applyExtremeMode() = coroutineScope.launch {
        disableBluetooth()
        disableGPS()
        killBackgroundProcesses()
        brightnessManager.changeBrightness(55)
    }

    private fun disableGPS() {
        val provider = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        if (provider.contains("gps")) {
            val intent = Intent()
            intent.setClassName(
                "com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider"
            )
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE)
            intent.data = Uri.parse("3")
            context.sendBroadcast(intent)
        }
    }

    private fun disableBluetooth() = coroutineScope.launch {
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter.isEnabled) bluetoothAdapter.disable()
        } catch (e: Exception) {
            Logger.logDebug("FUCK")
        }
    }

    private fun killBackgroundProcesses() = coroutineScope.launch {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(0)

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (packageInfo in packages) {
            if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1) continue
            if (packageInfo.packageName == "spiral.bit.dev.cleanmyandroid") continue
            activityManager.killBackgroundProcesses(packageInfo.packageName)
        }
    }

    sealed class State {
        data class NotOptimized(
            val batteryInfo: BatteryProvider.State,
            val currentModeTime: Pair<Int, Int>,
            val optimizedModeTime: Pair<Int, Int>
        ) : State()

        data class Optimized(
            val batteryInfo: BatteryProvider.State,
            val batteryOptimizationMode: BatteryOptimizationMode,
            val currentModeTime: Pair<Int, Int>,
            val optimizedModeTime: Pair<Int, Int>
        ) : State()

        data class Optimizing(val currentPercent: Int, val operationType: OperationType) : State()
        class ReadingData : State()
    }

    enum class OperationType {
        OPTIMIZING_BATTERY_USAGE,
        CHECKING_CPU,
        BOOST_BATTERY
    }

    enum class BatteryOptimizationMode {
        NORMAL_MODE,
        ULTRA_MODE,
        EXTREME_MODE
    }

    companion object {
        private const val SHORT_DELAY = 100L
        private const val LONG_DELAY = 1000L
        private const val ONE_HOUR = 60
    }
}