package runtime.exception.cleanmyandroid.base.extensions

import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.core.widget.doOnTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
fun EditText.textChanges(editTextConfig: EditTextConfig? = null): Flow<CharSequence?> {
    return callbackFlow {
        val listener = doOnTextChanged { text, _, _, _ ->
            editTextConfig?.let { config ->
                setCompoundButtonIcon(config)
            }
            trySend(text)
        }
        awaitClose { removeTextChangedListener(listener) }
    }
}

private fun EditText.setCompoundButtonIcon(editTextConfig: EditTextConfig) {
    if (editTextConfig.drawableGravity.constantValue == DrawableGravity.START.constantValue) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            editTextConfig.drawableIcon,
            NONE_DRAWABLE,
            NONE_DRAWABLE,
            NONE_DRAWABLE
        )
    } else if (editTextConfig.drawableGravity.constantValue == DrawableGravity.END.constantValue){
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            NONE_DRAWABLE,
            NONE_DRAWABLE,
            editTextConfig.drawableIcon,
            NONE_DRAWABLE
        )
    }
}

enum class DrawableGravity(val constantValue: Int) { START(0), END(2) }
class EditTextConfig(val drawableGravity: DrawableGravity, @DrawableRes val drawableIcon: Int)

private const val NONE_DRAWABLE = 0
