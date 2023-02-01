package runtime.exception.cleanmyandroid.ui.home.models

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.customViews.SliderView
import runtime.exception.cleanmyandroid.databinding.SliderItemBinding
import runtime.exception.cleanmyandroid.ui.home.HomeViewModel.*

fun itemHomeFeatureDelegate(onUnlockListener: (FeatureItem) -> Unit) =
    adapterDelegateViewBinding<FeatureItem, FeatureItem, SliderItemBinding>({ inflater, container ->
        SliderItemBinding.inflate(inflater, container, false)
    }) {
        with(binding) {
            bind {
                titleTextView.text = when (item) {
                    is FeatureItem.Battery -> getString(R.string.battery_feature_item_title)
                    is FeatureItem.Boost -> getString(R.string.boost_feature_item_title)
                    is FeatureItem.Memory -> getString(R.string.memory_feature_item_title)
                    is FeatureItem.Threats -> getString(R.string.threats_feature_item_title)
                }

                descriptionTextView.text = when (val item = item) {
                    is FeatureItem.Battery -> {
                        getString(R.string.battery_feature_item_description)
                    }
                    is FeatureItem.Boost -> {
                        getString(R.string.boost_feature_item_desciption)
                    }
                    is FeatureItem.Memory -> {
                        if (item.isCompleted) {
                            getString(R.string.memory_all_cleaned_description)
                        } else {
                            getString(R.string.memory_feature_item_description, item.megabytes)
                        }
                    }
                    is FeatureItem.Threats -> {
                        if (item.isCompleted) {
                            getString(R.string.threats_removed_description)
                        } else {
                            getString(R.string.threats_feature_item_description, item.appsCount)
                        }
                    }
                }

                when {
                    item.isEnabled && !item.isCompleted -> {
                        slideToUnlockView.state = SliderView.State.Available(
                            thumbResource = when (item) {
                                is FeatureItem.Battery -> R.drawable.battery_optimization_thumb
                                is FeatureItem.Boost -> R.drawable.device_boost_thumb
                                is FeatureItem.Memory -> R.drawable.smart_clean_thumb
                                is FeatureItem.Threats -> R.drawable.threats_protection_thumb
                            },
                            trackBackground = R.drawable.home_slider_background,
                            iconResource = R.drawable.ic_slide_green,
                            onSlideListener = {
                                onUnlockListener.invoke(item)
                            }
                        )
                    }
                    item.isCompleted -> {
                        slideToUnlockView
                        slideToUnlockView.state = SliderView.State.Completed(
                            drawablePadding = 16,
                            textColor = R.color.green,
                            trackBackground = R.drawable.home_slider_background,
                            title = R.string.ok_label,
                            iconResource = R.drawable.ic_done
                        )
                    }
                    else -> {
                        slideToUnlockView.state = SliderView.State.Locked(
                            iconResource = R.drawable.ic_lock_slider,
                            thumbResource = when (item) {
                                is FeatureItem.Battery -> R.drawable.battery_optimization_thumb
                                is FeatureItem.Boost -> R.drawable.device_boost_thumb
                                is FeatureItem.Memory -> R.drawable.smart_clean_thumb
                                is FeatureItem.Threats -> R.drawable.threats_protection_thumb
                            },
                            trackBackground = R.drawable.home_slider_background
                        )
                    }
                }
            }
        }
    }