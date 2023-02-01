package runtime.exception.cleanmyandroid.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import runtime.exception.cleanmyandroid.common.formatters.GigabytesFormatter
import runtime.exception.cleanmyandroid.common.formatters.MegabytesFormatter
import runtime.exception.cleanmyandroid.common.formatters.PercentageFormatter

val formattersModule = module {
    factory { GigabytesFormatter(context = androidContext()) }
    factory { MegabytesFormatter(context = androidContext()) }
    factory { PercentageFormatter(context = androidContext()) }
}