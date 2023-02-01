package runtime.exception.cleanmyandroid.common.data

import androidx.annotation.DrawableRes
import runtime.exception.cleanmyandroid.common.managers.NotificationContentType

data class Notification(
    val notificationContentType: NotificationContentType,
    val title: String,
    val description: String,
    val accentButtonText: String,
    val ignoreButtonText: String,
    @DrawableRes val icon: Int
)