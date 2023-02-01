package runtime.exception.cleanmyandroid.common

import kotlin.math.roundToInt

object PercentageConverter {

    fun getPercentageFromNumber(number: Int, otherNumber: Int): Int {
        return ((number.toFloat() / otherNumber) * 100).roundToInt()
    }
}