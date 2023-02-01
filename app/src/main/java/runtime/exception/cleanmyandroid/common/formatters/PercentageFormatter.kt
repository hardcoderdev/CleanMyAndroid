package runtime.exception.cleanmyandroid.common.formatters

import android.content.Context
import runtime.exception.cleanmyandroid.R

class PercentageFormatter(private val context: Context) {

    fun format(percentNumber: Int, isAddUnitLabel: Boolean = true): String {
        return if (isAddUnitLabel) context.getString(R.string.percentage_format, percentNumber)
        else context.getString(R.string.percentage_without_percent_format, percentNumber)
    }
}