package runtime.exception.cleanmyandroid.ui.threatProtection

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceProtector
import runtime.exception.cleanmyandroid.common.managers.InterstitialAdManager
import runtime.exception.cleanmyandroid.databinding.ThreatProtectionFragmentBinding
import java.util.concurrent.TimeUnit

class ThreatProtectionFragment : Fragment(R.layout.threat_protection_fragment) {

    private val interstitialAdManager by inject<InterstitialAdManager>()
    private val binding by viewBinding<ThreatProtectionFragmentBinding>()
    private val viewModel by viewModel<ThreatProtectionViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition(100, TimeUnit.MILLISECONDS)
        subscribeToObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.interruptProtectDevice()
    }

    private fun subscribeToObservers() = with(binding) {
        viewModel.events.launchAndCollectIn(viewLifecycleOwner) { event ->
            when (event) {
                is ThreatProtectionViewModel.ThreatProtectionEvents.NavigateToThreatsList -> {
                    findNavController().navigate(
                        R.id.threatsListFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .build()
                    )
                }
                is ThreatProtectionViewModel.ThreatProtectionEvents.NavigateToBatteryOptimization -> {
                    findNavController().navigate(
                        R.id.optimizeBatteryFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .build()
                    )
                }
            }
        }

        viewModel.state.filterNotNull().launchAndCollectIn(viewLifecycleOwner) {
            when (val deviceProtectorState = it.deviceProtectorState) {
                is DeviceProtector.State.Cleaned -> {
                    val color = ResourcesCompat.getColor(resources, R.color.green, null)

                    notScannedAppsTextView.setTextColor(color)
                    potentialThreatsTextView.setTextColor(color)
                    scanStateTextView.setTextColor(color)

                    scanStateTextView.text =
                        getString(R.string.scan_not_required_label, deviceProtectorState.previousScanAppsCount)
                    notScannedAppsTextView.text =
                        getString(R.string.not_scanned_format, 0)
                    potentialThreatsTextView.text =
                        getString(R.string.potential_threats_format, 0)

                    scanSlideToUnlock.state = SliderView.State.Completed(
                        drawablePadding = 24,
                        iconResource = R.drawable.ic_check_white,
                        trackBackground = R.drawable.button_background,
                        title = R.string.all_threats_are_removed_label,
                        textColor = R.color.white
                    )
                }
                is DeviceProtector.State.Cleaning -> {
                    if (deviceProtectorState.currentPercent == 99) {
                        interstitialAdManager.show(requireActivity())
                    }
                    operationProgressView.isVisible = true
                    operationProgressView.state = if (deviceProtectorState.currentPercent < 100) {
                        OperationProgressView.State.Running(
                            progress = deviceProtectorState.currentPercent,
                            operationTitle = when (deviceProtectorState.operationType) {
                                DeviceProtector.OperationType.UPDATE_THREAT_DB -> resources.getStringArray(R.array.smart_protection)[0]
                                DeviceProtector.OperationType.CHECKING_SECURITY_SETTINGS -> resources.getStringArray(R.array.smart_protection)[1]
                                DeviceProtector.OperationType.DETECT_THREAT_APP -> resources.getStringArray(R.array.smart_protection)[2]
                            }
                        )
                    } else {
                        OperationProgressView.State.Finished(
                            title = getString(R.string.threat_protection_completed),
                            accentButtonText = getString(R.string.threat_protection_finish),
                            accentButtonIcon = R.drawable.ic_threat_protection,
                            secondaryButtonText = getString(R.string.remove_threats_label),
                            onAccentButtonClick = {
                                viewModel.navigateToBatteryOptimization()
                            },
                            onSecondaryButtonClick = {
                                viewModel.navigateToThreatsList()
                            }
                        )
                    }
                }
                is DeviceProtector.State.Dirty -> {
                    operationProgressView.isVisible = false
                    val color = ResourcesCompat.getColor(resources, R.color.another_red, null)

                    notScannedAppsTextView.setTextColor(color)
                    potentialThreatsTextView.setTextColor(color)
                    scanStateTextView.setTextColor(color)

                    scanStateTextView.text = getString(R.string.required_threats_scan_format, deviceProtectorState.threatApps.size)

                    notScannedAppsTextView.text =
                        getString(R.string.not_scanned_format, deviceProtectorState.installedAppsCount)
                    potentialThreatsTextView.text =
                        getString(R.string.potential_threats_format, deviceProtectorState.threatApps.size)

                    scanSlideToUnlock.state = SliderView.State.Available(
                        thumbResource = R.drawable.thumb_scan_now,
                        iconResource = R.drawable.ic_slide_green,
                        trackBackground = R.drawable.slider_start_background,
                        onSlideListener = {
                            viewModel.cleanThreats()
                        }
                    )
                }
                is DeviceProtector.State.Reading -> {
                    operationProgressView.isVisible = false
                    scanSlideToUnlock.state = SliderView.State.Available(
                        thumbResource = R.drawable.thumb_scan_now,
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