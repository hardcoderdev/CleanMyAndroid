package runtime.exception.cleanmyandroid.ui.threatsList.models

import android.content.pm.PackageManager
import android.os.Parcelable
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import kotlinx.parcelize.Parcelize
import runtime.exception.cleanmyandroid.databinding.ThreatsItemBinding
import java.io.Serializable

@Parcelize
data class ItemThreatApp(
    val appIconDrawable: Int,
    val appName: String,
    val packageName: String
) : Serializable, Parcelable

fun harmfullyAppsDelegate(packageManager: PackageManager) =
    adapterDelegateViewBinding<ItemThreatApp, ItemThreatApp, ThreatsItemBinding>({ inflater, container ->
        ThreatsItemBinding.inflate(inflater, container, false)
    }) {
        with(binding) {
            bind {
                appIconImageView.setImageDrawable(
                    packageManager.getPackageInfo(
                        item.packageName,
                        0
                    ).applicationInfo.loadIcon(packageManager)
                )
                appNameTextView.text = item.appName
            }
        }
    }