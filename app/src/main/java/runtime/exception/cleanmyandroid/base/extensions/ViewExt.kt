package runtime.exception.cleanmyandroid.base.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

fun View.getBitmap(): Bitmap? {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}