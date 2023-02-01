package runtime.exception.cleanmyandroid.common.preferences

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class CommonPreferenceDelegate<T>(val name: String? = null) :
    ReadWriteProperty<SharedPreferencesHolder, T> {
    private fun getPreferenceKey(property: KProperty<*>) = name ?: property.name

    final override fun getValue(thisRef: SharedPreferencesHolder, property: KProperty<*>): T {
        return getValue(thisRef.sharedPreferences, getPreferenceKey(property))
    }

    final override fun setValue(thisRef: SharedPreferencesHolder, property: KProperty<*>, value: T) {
        setValue(thisRef.sharedPreferences, getPreferenceKey(property), value)
    }

    abstract fun getValue(prefs: SharedPreferences, key: String): T
    abstract fun setValue(prefs: SharedPreferences, key: String, value: T)
}
