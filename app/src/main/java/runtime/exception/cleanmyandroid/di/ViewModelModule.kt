package runtime.exception.cleanmyandroid.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import runtime.exception.cleanmyandroid.ui.deviceBoost.DeviceBoostViewModel
import runtime.exception.cleanmyandroid.ui.home.HomeViewModel
import runtime.exception.cleanmyandroid.ui.navigationContainer.NavigationContainerViewModel
import runtime.exception.cleanmyandroid.ui.optimizeBattery.OptimizeBatteryViewModel
import runtime.exception.cleanmyandroid.ui.smartCleaning.SmartCleaningViewModel
import runtime.exception.cleanmyandroid.ui.splash.SplashViewModel
import runtime.exception.cleanmyandroid.ui.threatProtection.ThreatProtectionViewModel
import runtime.exception.cleanmyandroid.ui.threatsList.ThreatsListViewModel
import runtime.exception.cleanmyandroid.ui.welcome.WelcomeViewModel

val viewModelModule = module {
    viewModel { SplashViewModel() }
    viewModel { WelcomeViewModel(deviceScanner = get()) }
    viewModel { NavigationContainerViewModel(deviceBooster = get(), deviceCleaner = get(), deviceProtector = get()) }
    viewModel { HomeViewModel(deviceBooster = get(), deviceCleaner = get(), deviceProtector = get(), deviceBatteryOptimizer = get()) }
    viewModel { DeviceBoostViewModel(deviceBooster = get()) }
    viewModel { OptimizeBatteryViewModel(deviceBatteryOptimizer = get()) }
    viewModel { SmartCleaningViewModel(deviceCleaner = get(), installedAppsProvider = get()) }
    viewModel { ThreatProtectionViewModel(deviceProtector = get()) }
    viewModel { ThreatsListViewModel(deviceProtector = get()) }
}