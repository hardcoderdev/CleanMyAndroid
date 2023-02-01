package runtime.exception.cleanmyandroid.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import runtime.exception.cleanmyandroid.common.features.boostFeatures.*

@OptIn(InternalCoroutinesApi::class)
val featuresModule = module {
    single {
        DeviceScanner(
            context = androidContext(),
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }
    single {
        DeviceBooster(
            context = androidContext(), coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }
    single {
        DeviceCleaner(
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }
    single {
        DeviceProtector(
            context = androidContext(),
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }
    single {
        DeviceBatteryOptimizer(
            context = androidContext(),
            coroutineScope = CoroutineScope(Dispatchers.IO),
            brightnessManager = get(),
            batteryProvider = get()
        )
    }
}
