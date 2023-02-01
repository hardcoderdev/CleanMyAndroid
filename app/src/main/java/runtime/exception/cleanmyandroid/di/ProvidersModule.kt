package runtime.exception.cleanmyandroid.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import runtime.exception.cleanmyandroid.common.providers.BatteryProvider
import runtime.exception.cleanmyandroid.common.providers.InstalledAppsProvider

val providersModule = module {
    single {
        InstalledAppsProvider(
            context = androidContext(),
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }
    single { BatteryProvider(context = androidContext()) }
}