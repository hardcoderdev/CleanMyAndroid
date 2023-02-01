package runtime.exception.cleanmyandroid.ui.deviceBoost

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.filterNotNull
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.customViews.OperationProgressView
import runtime.exception.cleanmyandroid.base.customViews.SliderView
import runtime.exception.cleanmyandroid.base.extensions.launchAndCollectIn
import runtime.exception.cleanmyandroid.common.ByteUnitConverter
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceBooster
import runtime.exception.cleanmyandroid.common.formatters.GigabytesFormatter
import runtime.exception.cleanmyandroid.common.managers.InterstitialAdManager
import runtime.exception.cleanmyandroid.databinding.DeviceBoostFragmentBinding
import java.util.concurrent.TimeUnit

class DeviceBoostFragment : Fragment(R.layout.device_boost_fragment) {

    private val interstitialManager by inject<InterstitialAdManager>()
    private val gigabytesFormatter by inject<GigabytesFormatter>()
    private val binding by viewBinding<DeviceBoostFragmentBinding>()
    private val viewModel by viewModel<DeviceBoostViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition(100, TimeUnit.MILLISECONDS)
        setUpClicks()
        subscribeToObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.interruptDeviceBoost()
    }

    private fun setUpClicks() = with(binding) {
        coolDownButton.setOnClickListener {
            viewModel.startBoosting()
        }
    }

    private fun subscribeToObservers() = with(viewModel) {
        events.launchAndCollectIn(
            viewLifecycleOwner,
            minActiveState = Lifecycle.State.RESUMED
        ) { event ->
            when (event) {
                is DeviceBoostViewModel.DeviceBoostEvents.NavigateToSmartCleaning -> {
                    findNavController().navigate(
                        R.id.smartCleaningFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .build()
                    )
                }
                is DeviceBoostViewModel.DeviceBoostEvents.NavigateToHome -> {
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }

        state.filterNotNull().launchAndCollectIn(viewLifecycleOwner) {
            with(binding) {
                when (val deviceBoosterState = it.deviceBoosterState) {
                    is DeviceBooster.State.Cleaned -> {
                        val color = ResourcesCompat.getColor(resources, R.color.green, null)
                        val deviceState = R.string.device_boost_not_required
                        val iconMemory = ResourcesCompat.getDrawable(resources, R.drawable.ic_menu_completed, null)
                        val iconCpu = ResourcesCompat.getDrawable(resources, R.drawable.ic_cpu_completed, null)
                        val iconLoading = ResourcesCompat.getDrawable(resources, R.drawable.ic_loading_completed, null)

                        iconProcessesImageView.setImageDrawable(iconMemory)
                        iconMemoryImageView.setImageDrawable(iconCpu)
                        iconLoadingImageView.setImageDrawable(iconLoading)

                        activeProcessesTextView.setTextColor(color)
                        memoryConsumptionTextView.setTextColor(color)
                        deviceStateTextView.setTextColor(color)

                        coolDownRequiredTextView.isVisible = false
                        coolDownButton.isEnabled = false
                        coolDownButton.alpha = 0.6f
                        cpuInCelsiusTextView.setTextColor(color)

                        activeProcessesInGbTextView.text = getString(
                            R.string.ratio_format,
                            gigabytesFormatter.format(ByteUnitConverter.fromMegabytesToGigabytes(deviceBoosterState.busyMegabytes)),
                            gigabytesFormatter.format(ByteUnitConverter.fromMegabytesToGigabytes(deviceBoosterState.totalMegabytes))
                        )

                        memoryConsumptionInGbTextView.text = getString(
                            R.string.ratio_format,
                            gigabytesFormatter.format(ByteUnitConverter.fromMegabytesToGigabytes(deviceBoosterState.busyMegabytes)),
                            gigabytesFormatter.format(ByteUnitConverter.fromMegabytesToGigabytes(deviceBoosterState.totalMegabytes))
                        )

                        cpuInCelsiusTextView.text = getString(R.string.celsius_format, deviceBoosterState.randomCpuInCelsius)

                        activeProcessesTextView.text = deviceBoosterState.runningProcesses.toString()
                        memoryConsumptionTextView.text = getString(R.string.percentage_format, deviceBoosterState.percentAvailable)

                        deviceStateTextView.text = getString(deviceState)

                        boostSlideToUnlock.state = SliderView.State.Completed(
                            drawablePadding = 24,
                            iconResource = R.drawable.ic_check_white,
                            trackBackground = R.drawable.button_background,
                            textColor = R.color.white,
                            title = R.string.optimized_label
                        )
                    }
                    is DeviceBooster.State.Cleaning -> {
                        if (deviceBoosterState.currentPercent == 99) {
                           interstitialManager.show(requireActivity())
                        }
                        operationProgressView.isVisible = true
                        operationProgressView.state = if (deviceBoosterState.currentPercent < 100) {
                            OperationProgressView.State.Running(
                                progress = deviceBoosterState.currentPercent,
                                operationTitle = when (deviceBoosterState.operationType) {
                                    DeviceBooster.OperationType.STOPPING_OLD_PROGRAMS -> resources.getStringArray(R.array.device_boost)[0]
                                    DeviceBooster.OperationType.OPTIMIZING_RAM -> resources.getStringArray(R.array.device_boost)[1]
                                    DeviceBooster.OperationType.CLEANING_CACHE -> resources.getStringArray(R.array.device_boost)[2]
                                }
                            )
                        } else {
                            OperationProgressView.State.Finished(
                                title = getString(R.string.device_boost_completed),
                                accentButtonText = getString(R.string.device_boost_finish),
                                accentButtonIcon = R.drawable.ic_smart_cleaning,
                                secondaryButtonText = getString(R.string.ok_label),
                                onAccentButtonClick = {
                                    viewModel.navigateToSmartCleaning()
                                },
                                onSecondaryButtonClick = {
                                    viewModel.navigateToHome()
                                }
                            )
                        }
                    }
                    is DeviceBooster.State.Dirty -> {
                        operationProgressView.isVisible = false
                        val color = ResourcesCompat.getColor(resources, R.color.another_red, null)
                        val deviceState = R.string.device_boost_required_label
                        val iconMemory = ResourcesCompat.getDrawable(resources, R.drawable.ic_menu, null)
                        val iconCpu = ResourcesCompat.getDrawable(resources, R.drawable.ic_cpu, null)
                        val iconLoading = ResourcesCompat.getDrawable(resources, R.drawable.ic_loading, null)

                        iconProcessesImageView.setImageDrawable(iconMemory)
                        iconMemoryImageView.setImageDrawable(iconCpu)
                        iconLoadingImageView.setImageDrawable(iconLoading)

                        activeProcessesTextView.setTextColor(color)
                        memoryConsumptionTextView.setTextColor(color)
                        deviceStateTextView.setTextColor(color)

                        coolDownRequiredTextView.isVisible = true
                        coolDownButton.isEnabled = true
                        coolDownButton.alpha = 1f
                        cpuInCelsiusTextView.setTextColor(color)

                        activeProcessesInGbTextView.text = getString(
                            R.string.ratio_format,
                            gigabytesFormatter.format(ByteUnitConverter.fromMegabytesToGigabytes(deviceBoosterState.busyMegabytes)),
                            gigabytesFormatter.format(ByteUnitConverter.fromMegabytesToGigabytes(deviceBoosterState.totalMegabytes))
                        )

                        memoryConsumptionInGbTextView.text = getString(
                            R.string.ratio_format,
                            gigabytesFormatter.format(ByteUnitConverter.fromMegabytesToGigabytes(deviceBoosterState.busyMegabytes)),
                            gigabytesFormatter.format(ByteUnitConverter.fromMegabytesToGigabytes(deviceBoosterState.totalMegabytes))
                        )

                        cpuInCelsiusTextView.text = getString(R.string.celsius_format, deviceBoosterState.randomCpuInCelsius)

                        activeProcessesTextView.text = deviceBoosterState.runningProcesses.toString()
                        memoryConsumptionTextView.text = getString(R.string.percentage_format, deviceBoosterState.percentAvailable)

                        deviceStateTextView.text = getString(deviceState)

                        boostSlideToUnlock.state = SliderView.State.Available(
                            thumbResource = R.drawable.boost_thumb,
                            iconResource = R.drawable.ic_slide_green,
                            trackBackground = R.drawable.slider_start_background,
                            onSlideListener = {
                                viewModel.startBoosting()
                            }
                        )
                    }
                    is DeviceBooster.State.ReadingData -> {
                        operationProgressView.isVisible = false
                        boostSlideToUnlock.state = SliderView.State.Available(
                            thumbResource = R.drawable.boost_thumb,
                            iconResource = R.drawable.ic_slide_green,
                            trackBackground = R.drawable.slider_start_background,
                            onSlideListener = {
                                Toast.makeText(requireContext(), "Analyse... Please, wait", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }
        }
    }
}