package runtime.exception.cleanmyandroid.common

object ByteUnitConverter {

    fun fromMegabytesToGigabytes(megabytes: Double): Double {
        return megabytes / 1024.0
    }
}