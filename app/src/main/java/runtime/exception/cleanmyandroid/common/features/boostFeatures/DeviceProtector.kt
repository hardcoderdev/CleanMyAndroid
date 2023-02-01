package runtime.exception.cleanmyandroid.common.features.boostFeatures

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import runtime.exception.cleanmyandroid.base.extensions.isSystemPackage
import runtime.exception.cleanmyandroid.ui.threatsList.models.ItemThreatApp

class DeviceProtector(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    private var cleanJob: Job? = null
    private val mutableState = MutableStateFlow<State>(State.Reading())
    val state = mutableState.asStateFlow()

    init {
        load()
    }

    private fun load() = coroutineScope.launch {
        val installedAppsCountDeferred = getPotentialHarmfullyApps()

        mutableState.update {
            State.Dirty(
                installedAppsCountDeferred.first,
                installedAppsCountDeferred.second,
                installedAppsCountDeferred.second
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
                            mutableState.update { State.Cleaning(percent, OperationType.UPDATE_THREAT_DB) }
                        }
                        percent <= 50 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.CHECKING_SECURITY_SETTINGS) }
                        }
                        percent <= 75 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.DETECT_THREAT_APP) }
                        }
                        percent <= 100 -> {
                            mutableState.update { State.Cleaning(percent, OperationType.DETECT_THREAT_APP) }
                        }
                    }
                }

                val installedAppsCountDeferred = getPotentialHarmfullyApps()

                mutableState.update {
                    State.Cleaned(
                        installedAppsCountDeferred.first,
                        installedAppsCountDeferred.second,
                        installedAppsCountDeferred.second
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

    private suspend fun getPotentialHarmfullyApps(): Pair<ArrayList<ItemThreatApp>, Int> {
        val packageManager = context.packageManager
        val harmfullyAppsUi = arrayListOf<ItemThreatApp>()
        val allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        allApps.forEach {
            val installerPackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(it.packageName).installingPackageName
            } else {
                packageManager.getInstallerPackageName(it.packageName)
            }

            if (installerPackageName != "com.android.vending") {
                val packageInfo = packageManager.getPackageInfo(it.packageName, 0)
                val itemThreatApp = ItemThreatApp(
                    appIconDrawable = packageInfo.applicationInfo.icon,
                    appName = packageManager.getApplicationLabel(packageInfo.applicationInfo)
                        .toString(),
                    packageName = it.packageName
                )
                if (!packageInfo.isSystemPackage() && it.packageName != context.packageName) {
                    harmfullyAppsUi.add(itemThreatApp)
                }
            }
        }
        return Pair(harmfullyAppsUi, allApps.size)
    }

    sealed class State {
        data class Dirty(
            val threatApps: List<ItemThreatApp>,
            val installedAppsCount: Int,
            val previousScanAppsCount: Int
        ) : State()

        data class Cleaned(
            val threatApps: List<ItemThreatApp>,
            val installedAppsCount: Int,
            val previousScanAppsCount: Int
        ) : State()

        data class Cleaning(val currentPercent: Int, val operationType: OperationType) : State()
        class Reading : State()
    }

    enum class OperationType {
        UPDATE_THREAT_DB,
        CHECKING_SECURITY_SETTINGS,
        DETECT_THREAT_APP
    }

    companion object {
        private const val SHORT_DELAY = 100L
        private const val LONG_DELAY = 1000L
    }
}