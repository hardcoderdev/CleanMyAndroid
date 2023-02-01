package runtime.exception.cleanmyandroid.common.features.boostFeatures

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

class DeviceBooster(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    private var cleanJob: Job? = null
    private val mutableState = MutableStateFlow<State>(State.ReadingData())
    val state = mutableState.asStateFlow()
    private var processorCpuInCelsius = 0
    private var runningProcessesCount = 0
    private var busyGigs = 0.0
    private var totalGigs = 0.0
    private var percentAvailable = 0

    init {
        load()
    }

    fun clean() {
        if (state.value is State.Cleaning) return
        cleanJob = coroutineScope.launch {
            try {
                progressFlow().collect { percent ->
                    when {
                        percent <= 25 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.STOPPING_OLD_PROGRAMS) }
                        }
                        percent <= 50 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.OPTIMIZING_RAM) }
                        }
                        percent <= 75 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.CLEANING_CACHE) }
                        }
                        percent <= 100 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.CLEANING_CACHE) }
                        }
                    }
                }

                mutableState.update {
                    State.Cleaned(
                        processorCpuInCelsius.div(2),
                        runningProcessesCount.div(2),
                        busyGigs.div(2),
                        totalGigs,
                        percentAvailable.div(2)
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

    private fun load() = coroutineScope.launch {
        mutableState.update { State.ReadingData() }
        val processorCpuInCelsiusDeferred = async {
            getProcessorCpuInCelsius()
        }
        val runningProcessesDeferred = async {
            getRunningProcesses()
        }
        val memoryConsumptionDeferred = async {
            getMemoryConsumption()
        }

        processorCpuInCelsius = processorCpuInCelsiusDeferred.await()
        runningProcessesCount = runningProcessesDeferred.await()
        val memoryConsumption = memoryConsumptionDeferred.await()
        busyGigs = memoryConsumption.first
        totalGigs = memoryConsumption.second
        percentAvailable = memoryConsumption.third

        mutableState.update {
            State.Dirty(
                processorCpuInCelsius,
                runningProcessesCount,
                busyGigs,
                totalGigs,
                percentAvailable
            )
        }
    }

    fun interrupt() {
        cleanJob?.cancel()
        cleanJob = null
    }

    private suspend fun getRunningProcesses(): Int {
        return try {
            val process = Runtime.getRuntime().exec("/system/bin/ps")
            val reader = InputStreamReader(process.inputStream)
            val bufferedReader = BufferedReader(reader)
            var numRead: Int
            val buffer = CharArray(5000)
            val commandOutput = StringBuffer()
            while (bufferedReader.read(buffer).also { numRead = it } > 0) {
                commandOutput.append(buffer, 0, numRead)
            }
            bufferedReader.close()
            process.waitFor()
            val processesCount = commandOutput.toString().filter { it == '0' }.count()
            processesCount
        } catch (e: Throwable) {
            1
        }
    }

    private suspend fun getMemoryConsumption(): Triple<Double, Double, Int> {
        val memoryInfo = ActivityManager.MemoryInfo()

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        val availableGigs = (memoryInfo.availMem / 0x100000L).toDouble()
        val totalGigs = (memoryInfo.totalMem / 0x100000L).toDouble()
        val busyGigs = totalGigs - availableGigs
        val percentAvail = memoryInfo.availMem / memoryInfo.totalMem.toDouble() * 100.0

        return Triple(busyGigs, totalGigs, percentAvail.toInt())
    }

    private suspend fun getProcessorCpuInCelsius(): Int {
        return Random.nextInt(30, 50)
    }

    sealed class State {
        data class Dirty(
            val randomCpuInCelsius: Int,
            val runningProcesses: Int,
            val busyMegabytes: Double,
            val totalMegabytes: Double,
            val percentAvailable: Int
        ) : State()

        data class Cleaned(
            val randomCpuInCelsius: Int,
            val runningProcesses: Int,
            val busyMegabytes: Double,
            val totalMegabytes: Double,
            val percentAvailable: Int
        ) : State()

        data class Cleaning(val currentPercent: Int, val operationType: OperationType) : State()
        class ReadingData : State()
    }

    enum class OperationType {
        STOPPING_OLD_PROGRAMS,
        OPTIMIZING_RAM,
        CLEANING_CACHE
    }

    companion object {
        private const val SHORT_DELAY = 100L
        private const val LONG_DELAY = 1000L
    }
}