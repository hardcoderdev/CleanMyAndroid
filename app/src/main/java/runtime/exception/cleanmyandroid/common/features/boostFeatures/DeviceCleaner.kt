package runtime.exception.cleanmyandroid.common.features.boostFeatures

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class DeviceCleaner(private val coroutineScope: CoroutineScope) {

    private var cleanJob: Job? = null
    private val mutableState = MutableStateFlow<State>(State.ReadingData())
    val state = mutableState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        mutableState.update {
            State.Dirty(
                cacheFileMegabytes = Random.nextInt(30, 150),
                temporaryFileMegabytes = Random.nextInt(30, 150),
                residualFileMegabytes = Random.nextInt(20, 100),
                systemJunkMegabytes = Random.nextInt(60, 100),
                megabytes = Random.nextInt(52, 400)
            )
        }
    }

    fun clean() {
        if (state.value is State.Cleaning) return
        val dirtyMegabytes = (state.value as? State.Dirty)?.megabytes ?: 0
        cleanJob = coroutineScope.launch {
            try {
                progressFlow().collect { percent ->
                    when {
                        percent <= 25 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.CLEANING_CACHE) }
                        }
                        percent <= 50 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.REMOVE_TEMP_FILES) }
                        }
                        percent <= 75 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.OPTIMIZING_SPACE) }
                        }
                        percent <= 100 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.OPTIMIZING_SPACE) }
                        }
                    }
                }

                mutableState.update {
                    State.Cleaned(megabytes = dirtyMegabytes)
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

    sealed class State {
        data class Dirty(
            val cacheFileMegabytes: Int,
            val temporaryFileMegabytes: Int,
            val residualFileMegabytes: Int,
            val systemJunkMegabytes: Int,
            val megabytes: Int
        ) : State()

        class Cleaned(val megabytes: Int) : State()
        data class Cleaning(val currentPercent: Int, val operationType: OperationType) : State()
        class ReadingData : State()
    }

    enum class OperationType {
        CLEANING_CACHE,
        REMOVE_TEMP_FILES,
        OPTIMIZING_SPACE
    }

    companion object {
        private const val SHORT_DELAY = 100L
        private const val LONG_DELAY = 1000L
    }
}