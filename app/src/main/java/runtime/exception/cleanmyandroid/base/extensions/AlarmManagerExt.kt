package runtime.exception.cleanmyandroid.base.extensions

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build

fun AlarmManager.setExactCompat(time: Long, pendingIntent: PendingIntent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    } else {
        setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }
}