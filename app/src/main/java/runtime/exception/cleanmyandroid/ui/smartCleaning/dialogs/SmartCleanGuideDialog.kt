package runtime.exception.cleanmyandroid.ui.smartCleaning.dialogs

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import org.koin.android.ext.android.inject
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.customViews.SliderView
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceCleaner
import runtime.exception.cleanmyandroid.databinding.SmartCleanGuideDialogBinding

class SmartCleanGuideDialog : DialogFragment() {

    private val binding by viewBinding<SmartCleanGuideDialogBinding>(createMethod = CreateMethod.INFLATE)
    private val deviceCleaner by inject<DeviceCleaner>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        val bitmapDrawable = BitmapDrawable(resources, arguments?.getParcelable("bitmap") as? Bitmap)
        dialog?.window?.setBackgroundDrawable(bitmapDrawable)
        rootBackgroundImageView.alpha = 0.7f

        cleanSlideToUnlockView.state = SliderView.State.Available(
            thumbResource = R.drawable.thumb_clean,
            iconResource = R.drawable.ic_slide_green,
            trackBackground = R.drawable.slider_start_background,
            onSlideListener = {
                dismiss()
                deviceCleaner.clean()
            }
        )
    }

    override fun getTheme(): Int {
        return R.style.FullScreenDialog
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
    }
}