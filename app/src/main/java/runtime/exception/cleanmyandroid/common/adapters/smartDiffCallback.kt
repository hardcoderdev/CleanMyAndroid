package runtime.exception.cleanmyandroid.common.adapters

import androidx.recyclerview.widget.DiffUtil
import runtime.exception.cleanmyandroid.ui.smartCleaning.models.ItemApp

val smartDiffCallback = object : DiffUtil.ItemCallback<ItemApp>() {
    override fun areItemsTheSame(oldItem: ItemApp, newItem: ItemApp) =
        oldItem::class == newItem::class

    override fun areContentsTheSame(oldItem: ItemApp, newItem: ItemApp) =
        oldItem == newItem
}