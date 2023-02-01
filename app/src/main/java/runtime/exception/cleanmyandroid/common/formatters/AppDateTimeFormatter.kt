package runtime.exception.cleanmyandroid.common.formatters

import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AppDateTimeFormatter {

    fun formatItemTime(createdTime: Long): String? {
        return SimpleDateFormat.getInstance().format(createdTime)
    }

    fun formatCalendar(localDate: LocalDate): String? {
        return DateTimeFormatter.ofPattern(MONTH_FORMAT).format(localDate)
    }

    fun formatFile(file: File): File {
        return File(
            file,
            SimpleDateFormat(
                FILE_NAME_FORMAT,
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + FILE_EXTENSION
        )
    }

    companion object {
        const val FILE_NAME_FORMAT = "yy-MM-dd"
        const val FILE_EXTENSION = ".jpg"
        const val MONTH_FORMAT = "MMMM"
    }
}