package runtime.exception.cleanmyandroid.common.formatters

import android.content.Context
import runtime.exception.cleanmyandroid.R

class GigabytesFormatter(private val context: Context) {

    fun format(gigabytes: Double, addUnitLabel: Boolean = true): String {
        return if (addUnitLabel) context.getString(R.string.gigabyte_format, gigabytes)
        else context.getString(R.string.gigabyte_without_label_format, gigabytes)
    }
}