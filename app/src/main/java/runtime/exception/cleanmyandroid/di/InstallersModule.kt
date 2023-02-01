package runtime.exception.cleanmyandroid.di

import android.app.AlarmManager
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import runtime.exception.cleanmyandroid.common.installers.NotificationInstaller

val installersModule = module {
    single { androidContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    single {
        NotificationInstaller(
            context = androidContext(),
            alarmManager = get()
        )
    }
}