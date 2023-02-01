package runtime.exception.cleanmyandroid.common.features.boostFeatures

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import runtime.exception.cleanmyandroid.common.DiskUtils
import kotlin.random.Random

class DeviceScanner(private val context: Context, private val coroutineScope: CoroutineScope) {

    private val mutableState = MutableStateFlow<State>(State.Reading(Random.nextInt(80, 89)))
    val state = mutableState.asStateFlow()
    private var cleanJob: Job? = null

    init {
        load()
    }

    private fun load() = coroutineScope.launch {
        val freeSpaceDeferred = async {
            calculateFreeSpace()
        }

        val freeSpace = freeSpaceDeferred.await()

        mutableState.update {
            State.Dirty(
                getUsedSpaceInPercent(),
                freeSpace,
                getMemoryConsumption()
            )
        }
    }

    fun clean() {
        if (state.value is State.Cleaning) return
        cleanJob = coroutineScope.launch {
            try {
                progressFlow().collect { percent ->
                    when {
                        percent <= 25 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.CHECKING_APPS) }
                        }
                        percent <= 50 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.UPDATING_THREATS_DB) }
                        }
                        percent <= 75 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.CHECKING_CPU) }
                        }
                        percent <= 100 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.CHECKING_CPU) }
                        }
                    }
                }
                val freeSpaceDeferred = async {
                    calculateFreeSpace()
                }

                mutableState.update {
                    State.Cleaned(
                        getUsedSpaceInPercent(),
                        freeSpaceDeferred.await(),
                        getMemoryConsumption()
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

    private suspend fun calculateFreeSpace(): Int {
        return DiskUtils.freeSpace(external = false)
    }

    private suspend fun getMemoryConsumption(): Double {
        val memoryInfo = ActivityManager.MemoryInfo()

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        return (memoryInfo.availMem / 0x100000L).toDouble()
    }

    private suspend fun getUsedSpaceInPercent(): Int {
        return Random.nextInt(80, 89)
    }

    sealed class State {
        data class Dirty(
            val percentageSpace: Int,
            val freeSpace: Int,
            val freeMemoryInMegabytes: Double
        ) : State()

        data class Cleaned(
            val percentageSpace: Int,
            val freeSpace: Int,
            val freeMemoryInMegabytes: Double
        ) : State()

        data class Cleaning(val currentPercent: Int, val operationType: OperationType) : State()
        class Reading(val percentageSpace: Int) : State()
    }

    enum class OperationType {
        CHECKING_APPS,
        UPDATING_THREATS_DB,
        CHECKING_CPU
    }

    companion object {
        private const val SHORT_DELAY = 100L
        private const val LONG_DELAY = 1000L
    }
}