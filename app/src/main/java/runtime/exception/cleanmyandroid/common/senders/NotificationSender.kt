package runtime.exception.cleanmyandroid.common.senders

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification.VISIBILITY_PUBLIC
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.common.data.Notification
import runtime.exception.cleanmyandroid.common.receivers.CancelNotificationReceiver
import runtime.exception.cleanmyandroid.ui.main.MainActivity

class NotificationSender(private val context: Context, private val notificationManagerCompat: NotificationManagerCompat) {

    @SuppressLint("UnspecifiedImmutableFlag")
    fun send(notification: Notification) = with(notification) {
        createChannelAndGroup()
        val notificationIntent = Intent(context, MainActivity::class.java)
        val closePushIntent = Intent(context, CancelNotificationReceiver::class.java).putExtra("notification_id", notification.notificationContentType.notificationId)
        val closePushPendingIntent = PendingIntent.getBroadcast(context, notification.notificationContentType.notificationId, closePushIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val contentIntent = PendingIntent.getActivity(context, notification.notificationContentType.notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_report_problem_black_24dp)
            .setContentTitle(title)
            .setContentText(description)
            .setChannelId(CHANNEL_ID)
            .setGroup(GROUP_NAME)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setContentIntent(contentIntent)
            .addAction(0, accentButtonText, contentIntent)
            .addAction(0, ignoreButtonText, closePushPendingIntent)
            .setAutoCancel(true)
        notificationManagerCompat.notify(notification.notificationContentType.notificationId, builder.build())
    }

    private fun createChannelAndGroup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
            createGroup()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.description = CHANNEL_DESCRIPTION
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
        channel.lockscreenVisibility = VISIBILITY_PUBLIC
        notificationManagerCompat.createNotificationChannel(channel)
    }

    private fun createGroup() {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "-100")
            .setContentInfo(CHANNEL_NAME)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setChannelId(CHANNEL_ID)
            .setGroup(GROUP_NAME)
            .setAutoCancel(true)
            .setGroupSummary(true)
        notificationManagerCompat.notify(GROUP_ID, builder.build())
    }

    companion object {
        private const val GROUP_ID = 22
        private const val GROUP_NAME = "system_booster"
        private const val CHANNEL_DESCRIPTION = "System booster channel description"
        private const val CHANNEL_ID = "SYSTEM_BOOSTER"
        private const val CHANNEL_NAME = "System Booster Channel"
    }
}