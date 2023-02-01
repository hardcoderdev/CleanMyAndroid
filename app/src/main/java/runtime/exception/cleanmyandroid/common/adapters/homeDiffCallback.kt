package runtime.exception.cleanmyandroid.common.adapters

import androidx.recyclerview.widget.DiffUtil
import runtime.exception.cleanmyandroid.ui.home.HomeViewModel.*

val diffCallback = object : DiffUtil.ItemCallback<FeatureItem>() {
    override fun areItemsTheSame(oldItem: FeatureItem, newItem: FeatureItem) =
        oldItem::class == newItem::class

    override fun areContentsTheSame(oldItem: FeatureItem, newItem: FeatureItem) =
        oldItem == newItem
}