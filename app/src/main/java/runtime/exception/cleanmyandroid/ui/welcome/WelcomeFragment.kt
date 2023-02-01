package runtime.exception.cleanmyandroid.ui.welcome

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.customViews.OperationProgressView
import runtime.exception.cleanmyandroid.base.customViews.SliderView
import runtime.exception.cleanmyandroid.base.extensions.launchAndCollectIn
import runtime.exception.cleanmyandroid.common.ByteUnitConverter
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceScanner
import runtime.exception.cleanmyandroid.common.formatters.GigabytesFormatter
import runtime.exception.cleanmyandroid.common.formatters.PercentageFormatter
import runtime.exception.cleanmyandroid.common.managers.InterstitialAdManager
import runtime.exception.cleanmyandroid.databinding.WelcomeFragmentBinding

class WelcomeFragment : Fragment(R.layout.welcome_fragment) {

    private val interstitialManager by inject<InterstitialAdManager>()
    private val gigabytesFormatter by inject<GigabytesFormatter>()
    private val percentageFormatter by inject<PercentageFormatter>()
    private val binding by viewBinding<WelcomeFragmentBinding>()
    private val viewModel by viewModel<WelcomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
    }

    private fun subscribeToObservers() = with(binding) {
        viewModel.events.onEach { event ->
            when (event) {
                is WelcomeViewModel.WelcomeEvents.NavigateToHome -> {
                    findNavController().navigate(R.id.navigationContainerFragment)
                }
                is WelcomeViewModel.WelcomeEvents.ShowBanner -> {
                    binding.adView.loadAd(event.adRequest)
                }
            }
        }.launchIn(lifecycleScope)

        viewModel.state.filterNotNull().launchAndCollectIn(viewLifecycleOwner) {
            when (val deviceScannerState = it.deviceScannerState) {
                is DeviceScanner.State.Cleaned -> {
                    freeMemoryTextView.text = gigabytesFormatter.format(
                        ByteUnitConverter.fromMegabytesToGigabytes(deviceScannerState.freeMemoryInMegabytes),
                    false
                    )

                    usedMemoryTextView.text = percentageFormatter.format(deviceScannerState.percentageSpace, false)
                }
                is DeviceScanner.State.Cleaning -> {
                    if (deviceScannerState.currentPercent == 99) {
                        interstitialManager.show(requireActivity())
                    }
                    operationProgressView.isVisible = true
                    operationProgressView.state = OperationProgressView.State.Running(
                        progress = deviceScannerState.currentPercent,
                        operationTitle = when (deviceScannerState.operationType) {
                            DeviceScanner.OperationType.CHECKING_APPS -> resources.getStringArray(R.array.scan_device)[0]
                            DeviceScanner.OperationType.UPDATING_THREATS_DB -> resources.getStringArray(R.array.smart_clean)[1]
                            DeviceScanner.OperationType.CHECKING_CPU -> resources.getStringArray(R.array.smart_clean)[2]
                        }
                    )
                }
                is DeviceScanner.State.Dirty -> {
                    freeMemoryTextView.text = gigabytesFormatter.format(
                        ByteUnitConverter.fromMegabytesToGigabytes(deviceScannerState.freeMemoryInMegabytes),
                        false
                    )

                    usedMemoryTextView.text = percentageFormatter.format(deviceScannerState.percentageSpace, false)

                    sliderView.state = SliderView.State.Available(
                        thumbResource = R.drawable.start_slider_thumb,
                        iconResource = R.drawable.ic_slide,
                        trackBackground = R.drawable.slider_start_background,
                        onSlideListener = {
                            viewModel.calculateMemorySpace()
                        }
                    )
                }
                is DeviceScanner.State.Reading -> {
                    usedMemoryTextView.text = percentageFormatter.format(deviceScannerState.percentageSpace, false)
                }
            }
        }
    }
}