package runtime.exception.cleanmyandroid.common.managers

import android.content.Context
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.common.data.Notification

enum class NotificationContentType(val notificationId: Int) {
    TRY_CLEAR_CACHE(0),
    REDUCE_APPS(1),
    OPTIMIZE_APPS(2),
    CPU_DESCRIPTION(3),
    RAM_USAGE(4),
    SLOWER_PHONE(5)
}

class NotificationContentManager(private val context: Context) {

    fun getNotification(notificationContentType: NotificationContentType): Notification {
        return when (notificationContentType) {
            NotificationContentType.TRY_CLEAR_CACHE -> Notification(
                title = context.getString(R.string.system_booster_label),
                description = context.getString(R.string.try_clear_cache_description),
                accentButtonText = context.getString(R.string.clean_label),
                ignoreButtonText = context.getString(R.string.close_label),
                icon = R.drawable.app_logo,
                notificationContentType = notificationContentType
            )
            NotificationContentType.REDUCE_APPS -> Notification(
                title = context.getString(R.string.chargement_enhacement_label),
                description = context.getString(R.string.reduce_apps_description),
                accentButtonText = context.getString(R.string.optimize_label),
                ignoreButtonText = context.getString(R.string.later_label),
                icon = R.drawable.app_logo,
                notificationContentType = notificationContentType
            )
            NotificationContentType.OPTIMIZE_APPS -> Notification(
                title = context.getString(R.string.system_booster_label),
                description = context.getString(R.string.optimize_apps_description),
                accentButtonText = context.getString(R.string.optimize_label),
                ignoreButtonText = context.getString(R.string.close_label),
                icon = R.drawable.app_logo,
                notificationContentType = notificationContentType
            )
            NotificationContentType.CPU_DESCRIPTION -> Notification(
                title = context.getString(R.string.cpu_booster_label),
                description = context.getString(R.string.cpu_description),
                accentButtonText = context.getString(R.string.rid_trash_label),
                ignoreButtonText = context.getString(R.string.close_label),
                icon = R.drawable.app_logo,
                notificationContentType = notificationContentType
            )
            NotificationContentType.RAM_USAGE -> Notification(
                title = context.getString(R.string.system_booster_label),
                description = context.getString(R.string.ram_usage_description),
                accentButtonText = context.getString(R.string.rid_trash_label),
                ignoreButtonText = context.getString(R.string.close_label),
                icon = R.drawable.app_logo,
                notificationContentType = notificationContentType
            )
            NotificationContentType.SLOWER_PHONE -> Notification(
                title = context.getString(R.string.system_booster_label),
                description = context.getString(R.string.slower_phone_description),
                accentButtonText = context.getString(R.string.rid_trash_label),
                ignoreButtonText = context.getString(R.string.ignore_label),
                icon = R.drawable.app_logo,
                notificationContentType = notificationContentType
            )
        }
    }
}