package runtime.exception.cleanmyandroid.ui.smartCleaning.models

import android.graphics.drawable.Drawable
import androidx.core.view.isVisible
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import runtime.exception.cleanmyandroid.databinding.AppItemBinding

data class ItemApp(
    val icon: Drawable,
    val version: String,
    val weight: Int,
    val appName: String,
    val packageName: String
)

fun itemAppDelegate(onDelete: (ItemApp) -> Unit) =
    adapterDelegateViewBinding<ItemApp, ItemApp, AppItemBinding>({ inflater, container ->
        AppItemBinding.inflate(inflater, container, false)
    }) {
        with(binding) {
            iconDeleteImageView.setOnClickListener { onDelete.invoke(item) }
            bind {
                appNameTextView.text = item.appName
                versionTextView.text = item.version
                appIconImageView.setImageDrawable(item.icon)
                if (item.packageName == "spiral.bit.dev.cleanmyandroid") {
                    iconDeleteImageView.isVisible = false
                }
            }
        }
    }