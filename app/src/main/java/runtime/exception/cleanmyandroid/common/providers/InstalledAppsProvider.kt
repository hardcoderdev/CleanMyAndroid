package runtime.exception.cleanmyandroid.common.providers

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import runtime.exception.cleanmyandroid.base.extensions.isSystemPackage
import runtime.exception.cleanmyandroid.ui.smartCleaning.models.ItemApp

class InstalledAppsProvider(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    private val mutableState = MutableStateFlow(State(emptyList()))
    val state = mutableState.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() = coroutineScope.launch(Dispatchers.IO) {
        val packageManager = context.packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val temp = arrayListOf<ItemApp>()
        apps.forEach {
            val packageInfo = packageManager.getPackageInfo(it.packageName, 0)
            val appName = packageManager.getApplicationLabel(it)
            val itemApp = ItemApp(
                icon = it.loadIcon(packageManager),
                version = packageInfo.versionName,
                weight = 0,
                appName = appName.toString(),
                packageName = packageInfo.packageName
            )
            if (!packageInfo.isSystemPackage()) temp.add(itemApp)
        }

        mutableState.update {
            State(apps = temp)
        }
    }

    data class State(val apps: List<ItemApp>)
}