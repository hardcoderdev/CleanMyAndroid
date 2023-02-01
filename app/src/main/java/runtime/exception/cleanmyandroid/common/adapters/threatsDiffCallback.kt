package runtime.exception.cleanmyandroid.common.adapters

import androidx.recyclerview.widget.DiffUtil
import runtime.exception.cleanmyandroid.ui.threatsList.models.ItemThreatApp

val threatsDiffCallback = object : DiffUtil.ItemCallback<ItemThreatApp>() {
    override fun areItemsTheSame(oldItem: ItemThreatApp, newItem: ItemThreatApp) =
        oldItem::class == newItem::class

    override fun areContentsTheSame(oldItem: ItemThreatApp, newItem: ItemThreatApp) =
        oldItem == newItem
}