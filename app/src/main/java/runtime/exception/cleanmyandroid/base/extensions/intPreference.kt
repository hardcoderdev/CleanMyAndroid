package runtime.exception.cleanmyandroid.base.extensions

import runtime.exception.cleanmyandroid.common.preferences.SharedPreferencesHolder
import runtime.exception.cleanmyandroid.common.preferences.delegates.*

fun SharedPreferencesHolder.intPreference(defValue: Int = 0, name: String? = null) =
    IntPreferenceDelegate(defValue, name)

fun SharedPreferencesHolder.stringPreference(defValue: String = "", name: String? = null) =
    StringPreferenceDelegate(defValue, name)

fun SharedPreferencesHolder.booleanPreference(defValue: Boolean = false, name: String? = null) =
    BooleanPreferenceDelegate(defValue, name)

fun SharedPreferencesHolder.floatPreference(defValue: Float = 0f, name: String? = null) =
    FloatPreferenceDelegate(defValue, name)

fun SharedPreferencesHolder.longPreference(defValue: Long = 0L, name: String? = null) =
    LongPreferenceDelegate(defValue, name)

fun SharedPreferencesHolder.stringSetPreference(defValue: MutableSet<String> = mutableSetOf(), name: String? = null) =
    StringSetPreferenceDelegate(defValue, name)