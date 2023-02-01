package runtime.exception.cleanmyandroid.common.preferences.delegates

import android.content.SharedPreferences
import runtime.exception.cleanmyandroid.common.preferences.CommonPreferenceDelegate

class FloatPreferenceDelegate(private val defValue: Float, name: String? = null) :
    CommonPreferenceDelegate<Float>(name) {
    override fun getValue(prefs: SharedPreferences, key: String) = prefs.getFloat(key, defValue)
    override fun setValue(prefs: SharedPreferences, key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }
}