package runtime.exception.cleanmyandroid.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import runtime.exception.cleanmyandroid.common.managers.NotificationContentManager
import runtime.exception.cleanmyandroid.common.managers.BrightnessManager
import runtime.exception.cleanmyandroid.common.managers.InterstitialAdManager

val managersModule = module {
    single { BrightnessManager(context = androidContext()) }
    single { InterstitialAdManager(context = androidContext()) }
    single { NotificationContentManager(context = androidContext()) }
}