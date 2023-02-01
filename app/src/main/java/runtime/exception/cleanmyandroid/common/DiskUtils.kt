package runtime.exception.cleanmyandroid.common

import android.os.StatFs
import android.os.Environment

object DiskUtils {
    private const val MEGA_BYTE: Long = 1048576

    /**
     * Calculates total space on disk
     * @param external  If true will query external disk, otherwise will query internal disk.
     * @return Number of mega bytes on disk.
     */
    fun totalSpace(external: Boolean): Int {
        val statFs = getStats(external)
        val total = statFs.blockCountLong * statFs.blockSizeLong / MEGA_BYTE
        return total.toInt()
    }

    /**
     * Calculates free space on disk
     * @param external  If true will query external disk, otherwise will query internal disk.
     * @return Number of free mega bytes on disk.
     */
    fun freeSpace(external: Boolean): Int {
        val statFs = getStats(external)
        val availableBlocks = statFs.availableBlocksLong
        val blockSize = statFs.blockSizeLong
        val freeBytes = availableBlocks * blockSize
        return (freeBytes / MEGA_BYTE).toInt()
    }

    /**
     * Calculates occupied space on disk
     * @param external  If true will query external disk, otherwise will query internal disk.
     * @return Number of occupied mega bytes on disk.
     */
    fun busySpace(external: Boolean): Int {
        val statFs = getStats(external)
        val total = statFs.blockCountLong * statFs.blockSizeLong
        val free = statFs.availableBlocksLong * statFs.blockSizeLong
        return ((total - free) / MEGA_BYTE).toInt()
    }

    private fun getStats(external: Boolean): StatFs {
        val path: String = if (external) {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            Environment.getRootDirectory().absolutePath
        }
        return StatFs(path)
    }
}