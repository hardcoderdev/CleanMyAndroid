package runtime.exception.cleanmyandroid.base.extensions

private const val MEGABYTE = 1024L * 1024L

fun Long.toMegabytes() = this / MEGABYTE