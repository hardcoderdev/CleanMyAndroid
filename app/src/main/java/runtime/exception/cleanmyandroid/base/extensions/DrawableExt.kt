package runtime.exception.cleanmyandroid.base.extensions

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt

fun Drawable.setColorFilterByColor(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
    } else {
        setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }
}