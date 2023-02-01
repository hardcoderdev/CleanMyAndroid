package runtime.exception.cleanmyandroid.common

import android.util.Log

object Logger {

    fun logDebug(msg: String) = Log.d(DEBUG_TAG, msg)

    fun logInfo(msg: String) = Log.i(INFO_TAG, msg)

    fun logVerbose(msg: String) = Log.v(VERBOSE_TAG, msg)

    fun logWarn(msg: String) = Log.w(WARN_TAG, msg)

    fun logError(throwable: Throwable?) = throwable?.localizedMessage?.let {
        Log.e(ERROR_TAG, it)
    } ?: run {
        "Error happens, throwable is null"
    }

    private const val DEBUG_TAG = "DEBUG_TAG"
    private const val INFO_TAG = "INFO_TAG"
    private const val VERBOSE_TAG = "VERBOSE_TAG"
    private const val WARN_TAG = "WARN_TAG"
    private const val ERROR_TAG = "ERROR_TAG"
}