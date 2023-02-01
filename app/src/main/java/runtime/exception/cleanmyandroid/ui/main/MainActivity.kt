package runtime.exception.cleanmyandroid.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.common.installers.NotificationInstaller
import runtime.exception.cleanmyandroid.common.managers.NotificationContentManager
import runtime.exception.cleanmyandroid.common.managers.NotificationContentType
import kotlin.random.Random
import android.content.Context

import android.content.SharedPreferences
import runtime.exception.cleanmyandroid.base.extensions.booleanPreference
import runtime.exception.cleanmyandroid.common.preferences.SharedPreferencesHolder

class MainActivity : AppCompatActivity(R.layout.main_activity), SharedPreferencesHolder {

    private var isAlarmAlreadySet by booleanPreference(defValue = false, name = IS_ALARM_ALREADY_SET)
    private val notificationContentManager: NotificationContentManager by inject()
    private val notificationInstaller by inject<NotificationInstaller>()
    override val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notificationType = NotificationContentType.values()[Random.nextInt(0, 5)]
        val notification = notificationContentManager.getNotification(notificationType)

        if (!isAlarmAlreadySet) {
            notificationInstaller.install(notification)
            isAlarmAlreadySet = true
        }
    }

    companion object {
        private const val IS_ALARM_ALREADY_SET = "is_alarm_already_set_key"
    }
}