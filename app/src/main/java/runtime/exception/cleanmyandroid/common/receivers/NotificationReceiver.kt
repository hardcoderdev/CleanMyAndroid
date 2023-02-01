package runtime.exception.cleanmyandroid.common.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import runtime.exception.cleanmyandroid.common.managers.NotificationContentManager
import runtime.exception.cleanmyandroid.common.managers.NotificationContentType
import runtime.exception.cleanmyandroid.common.senders.NotificationSender

class NotificationReceiver : BroadcastReceiver(), KoinComponent {

    private val notificationContentManager: NotificationContentManager by inject()
    private val notificationSender: NotificationSender by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", 0)
        val notification = notificationContentManager.getNotification(
            checkNotNull(
                NotificationContentType.values().find {
                    it.notificationId == notificationId
                }
            )
        )

        notificationSender.send(notification)
    }
}
