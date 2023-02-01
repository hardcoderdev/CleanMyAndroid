package runtime.exception.cleanmyandroid.common.installers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import runtime.exception.cleanmyandroid.common.data.Notification
import runtime.exception.cleanmyandroid.common.receivers.NotificationReceiver
import java.util.*

class NotificationInstaller(private val context: Context, private val alarmManager: AlarmManager) {

    fun install(notification: Notification) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notification.notificationContentType.notificationId)
        }

        val firstPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) it or PendingIntent.FLAG_IMMUTABLE
                else it
            }
        )

        val secondPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) it or PendingIntent.FLAG_IMMUTABLE
                else it
            }
        )

        val morningCalendar = Calendar.getInstance()
        morningCalendar.apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val eveningCalendar = Calendar.getInstance()
        eveningCalendar.apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            morningCalendar.timeInMillis,
            24 * 60 * 1000 * 60,
            firstPendingIntent
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            eveningCalendar.timeInMillis,
            24 * 60 * 1000 * 60,
            secondPendingIntent
        )
    }
}