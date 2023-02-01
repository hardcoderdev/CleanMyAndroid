package runtime.exception.cleanmyandroid.di

import androidx.core.app.NotificationManagerCompat
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import runtime.exception.cleanmyandroid.common.senders.NotificationSender

val sendersModule = module {
    single { NotificationManagerCompat.from(androidContext()) }
    single { NotificationSender(context = androidContext(), notificationManagerCompat = get()) }
}