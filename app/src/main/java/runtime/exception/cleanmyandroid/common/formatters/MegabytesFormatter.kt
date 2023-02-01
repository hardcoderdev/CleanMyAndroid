package runtime.exception.cleanmyandroid.common.formatters

import android.content.Context
import runtime.exception.cleanmyandroid.R

class MegabytesFormatter(private val context: Context) {

    fun format(megabytes: Int): String {
        return context.getString(R.string.megabytes_format, megabytes)
    }
}