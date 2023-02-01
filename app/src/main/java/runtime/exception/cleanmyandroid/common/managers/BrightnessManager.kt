package runtime.exception.cleanmyandroid.common.managers

import android.content.Context
import android.provider.Settings

class BrightnessManager(private val context: Context) {

    fun changeBrightness(brightness: Int) {
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
    }
}