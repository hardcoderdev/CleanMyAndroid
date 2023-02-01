package runtime.exception.cleanmyandroid.ui.optimizeBattery

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.filterNotNull
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.customViews.OperationProgressView
import runtime.exception.cleanmyandroid.base.extensions.launchAndCollectIn
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceBatteryOptimizer
import runtime.exception.cleanmyandroid.common.managers.InterstitialAdManager
import runtime.exception.cleanmyandroid.databinding.OptimizeBatteryFragmentBinding
import java.util.concurrent.TimeUnit

class OptimizeBatteryFragment : Fragment(R.layout.optimize_battery_fragment) {

    private val interstitialManager by inject<InterstitialAdManager>()
    private val binding by viewBinding<OptimizeBatteryFragmentBinding>()
    private val viewModel by viewModel<OptimizeBatteryViewModel>()
    private val goToSettingsCallback = registerForActivityResult(StartActivityForResult()) { /* no-op */ }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition(100, TimeUnit.MILLISECONDS)
        subscribeToObservers()
        setUpClicks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.interruptBatteryOptimize()
    }

    private fun setUpClicks() = with(binding) {
        batteryLinearLayout1.setOnClickListener {
            if (checkPermission()) {
                viewModel.optimizeBattery(DeviceBatteryOptimizer.BatteryOptimizationMode.NORMAL_MODE)
            } else {
                Toast.makeText(requireContext(), "We need this permission to work with the battery", Toast.LENGTH_LONG).show()
            }
        }

        batteryLinearLayout2.setOnClickListener {
            if (checkPermission()) {
                viewModel.optimizeBattery(DeviceBatteryOptimizer.BatteryOptimizationMode.ULTRA_MODE)
            } else {
                Toast.makeText(requireContext(), "We need this permission to work with the battery", Toast.LENGTH_LONG).show()
            }
        }

        batteryLinearLayout3.setOnClickListener {
            if (checkPermission()) {
                viewModel.optimizeBattery(DeviceBatteryOptimizer.BatteryOptimizationMode.EXTREME_MODE)
            } else {
                Toast.makeText(requireContext(), "We need this permission to work with the battery", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (!Settings.System.canWrite(requireContext())) {
            goToSettingsCallback.launch(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
            false
        } else true
    }

    private fun subscribeToObservers() = with(binding) {
        viewModel.events.launchAndCollectIn(viewLifecycleOwner) { event ->
            when (event) {
                is OptimizeBatteryViewModel.OptimizeBatteryEvents.NavigateToHome -> {
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }

        viewModel.state.filterNotNull().launchAndCollectIn(viewLifecycleOwner) {
            when (val deviceBatteryOptimizerState = it.deviceBatteryOptimizerState) {
                is DeviceBatteryOptimizer.State.NotOptimized -> {
                    operationProgressView.isVisible = false
                    percentageBatteryTextView.text =
                        getString(R.string.percentage_format, deviceBatteryOptimizerState.batteryInfo.percentageBatteryLevel)
                    currentBatteryStateTextView.text = getString(
                        R.string.time_hm_format,
                        deviceBatteryOptimizerState.currentModeTime.first,
                        deviceBatteryOptimizerState.currentModeTime.second
                    )
                    optimizedBatteryStateTextView.text = getString(
                        R.string.time_hm_format,
                        deviceBatteryOptimizerState.optimizedModeTime.first,
                        deviceBatteryOptimizerState.optimizedModeTime.second
                    )

                    hoursTextView1.text = getString(
                        R.string.time_h_format,
                        deviceBatteryOptimizerState.currentModeTime.first
                    )
                    minutesTextView1.text = getString(
                        R.string.time_m_format,
                        deviceBatteryOptimizerState.currentModeTime.second
                    )

                    hoursTextView2.text = getString(
                        R.string.time_h_format,
                        deviceBatteryOptimizerState.currentModeTime.first * 2
                    )
                    minutesTextView2.text = getString(
                        R.string.time_m_format,
                        deviceBatteryOptimizerState.currentModeTime.second * 2
                    )

                    hoursTextView3.text = getString(
                        R.string.time_h_format,
                        deviceBatteryOptimizerState.currentModeTime.first * 3
                    )
                    minutesTextView3.text = getString(
                        R.string.time_m_format,
                        deviceBatteryOptimizerState.currentModeTime.second * 3
                    )
                }
                is DeviceBatteryOptimizer.State.Optimized -> {
                    percentageBatteryTextView.text =
                        getString(R.string.percentage_format, deviceBatteryOptimizerState.batteryInfo.percentageBatteryLevel)
                    currentBatteryStateTextView.isVisible = false
                    optimizedBatteryStateTextView.text = when (deviceBatteryOptimizerState.batteryOptimizationMode) {
                        DeviceBatteryOptimizer.BatteryOptimizationMode.NORMAL_MODE -> {
                            getString(
                                R.string.time_hm_format,
                                deviceBatteryOptimizerState.optimizedModeTime.first,
                                deviceBatteryOptimizerState.optimizedModeTime.second
                            )
                        }
                        DeviceBatteryOptimizer.BatteryOptimizationMode.ULTRA_MODE -> {
                            getString(
                                R.string.time_hm_format,
                                deviceBatteryOptimizerState.optimizedModeTime.first * 2,
                                deviceBatteryOptimizerState.optimizedModeTime.second * 2
                            )
                        }
                        DeviceBatteryOptimizer.BatteryOptimizationMode.EXTREME_MODE -> {
                            getString(
                                R.string.time_hm_format,
                                deviceBatteryOptimizerState.optimizedModeTime.first * 3,
                                deviceBatteryOptimizerState.optimizedModeTime.second * 3
                            )
                        }
                    }

                    when (deviceBatteryOptimizerState.batteryOptimizationMode) {
                        DeviceBatteryOptimizer.BatteryOptimizationMode.NORMAL_MODE -> {
                            batteryLinearLayout1.setBackgroundResource(R.drawable.battery_on_ripple)
                            batteryLinearLayout2.setBackgroundResource(R.drawable.battery_off_ripple)
                            batteryLinearLayout3.setBackgroundResource(R.drawable.battery_off_ripple)
                        }
                        DeviceBatteryOptimizer.BatteryOptimizationMode.ULTRA_MODE -> {
                            batteryLinearLayout1.setBackgroundResource(R.drawable.battery_off_ripple)
                            batteryLinearLayout2.setBackgroundResource(R.drawable.battery_on_ripple)
                            batteryLinearLayout3.setBackgroundResource(R.drawable.battery_off_ripple)
                        }
                        DeviceBatteryOptimizer.BatteryOptimizationMode.EXTREME_MODE -> {
                            batteryLinearLayout1.setBackgroundResource(R.drawable.battery_off_ripple)
                            batteryLinearLayout2.setBackgroundResource(R.drawable.battery_off_ripple)
                            batteryLinearLayout3.setBackgroundResource(R.drawable.battery_on_ripple)
                        }
                    }

                    selectBatteryModeTextView.text = getString(R.string.battery_mode_applied)

                    when (deviceBatteryOptimizerState.batteryOptimizationMode) {
                        DeviceBatteryOptimizer.BatteryOptimizationMode.NORMAL_MODE -> {
                            hoursTextView1.text = getString(R.string.time_h_format, deviceBatteryOptimizerState.currentModeTime.first)
                            minutesTextView1.text = getString(R.string.time_m_format, deviceBatteryOptimizerState.currentModeTime.second)

                            hoursTextView2.text = getString(R.string.time_h_format, 0)
                            minutesTextView2.text = getString(R.string.time_m_format, 0)

                            hoursTextView3.text = getString(R.string.time_h_format, 0)
                            minutesTextView3.text = getString(R.string.time_m_format, 0)
                        }
                        DeviceBatteryOptimizer.BatteryOptimizationMode.ULTRA_MODE -> {
                            hoursTextView1.text = getString(R.string.time_h_format, 0)
                            minutesTextView1.text = getString(R.string.time_m_format, 0)

                            hoursTextView2.text = getString(R.string.time_h_format, deviceBatteryOptimizerState.currentModeTime.first * 2)
                            minutesTextView2.text = getString(R.string.time_m_format, deviceBatteryOptimizerState.currentModeTime.second * 2)

                            hoursTextView3.text = getString(R.string.time_h_format, 0)
                            minutesTextView3.text = getString(R.string.time_m_format, 0)
                        }
                        DeviceBatteryOptimizer.BatteryOptimizationMode.EXTREME_MODE -> {
                            hoursTextView1.text = getString(R.string.time_h_format, 0)
                            minutesTextView1.text = getString(R.string.time_m_format, 0)

                            hoursTextView2.text = getString(R.string.time_h_format, 0)
                            minutesTextView2.text = getString(R.string.time_m_format, 0)

                            hoursTextView3.text = getString(R.string.time_h_format, deviceBatteryOptimizerState.currentModeTime.first * 3)
                            minutesTextView3.text = getString(R.string.time_m_format, deviceBatteryOptimizerState.currentModeTime.second * 3)
                        }
                    }
                }
                is DeviceBatteryOptimizer.State.Optimizing -> {
                    if (deviceBatteryOptimizerState.currentPercent == 99) {
                        interstitialManager.show(requireActivity())
                    }
                    operationProgressView.isVisible = true
                    operationProgressView.state = if (deviceBatteryOptimizerState.currentPercent < 100) {
                        OperationProgressView.State.Running(
                            progress = deviceBatteryOptimizerState.currentPercent,
                            operationTitle = when (deviceBatteryOptimizerState.operationType) {
                                DeviceBatteryOptimizer.OperationType.OPTIMIZING_BATTERY_USAGE -> resources.getStringArray(R.array.battery_optimization)[0]
                                DeviceBatteryOptimizer.OperationType.CHECKING_CPU -> resources.getStringArray(R.array.battery_optimization)[1]
                                DeviceBatteryOptimizer.OperationType.BOOST_BATTERY -> resources.getStringArray(R.array.battery_optimization)[2]
                            }
                        )
                    } else {
                        OperationProgressView.State.Finished(
                            title = getString(R.string.battery_optimization_completed),
                            accentButtonText = getString(R.string.battery_optimization_finish),
                            accentButtonIcon = R.drawable.ic_battery_optimization,
                            secondaryButtonText = getString(R.string.ok_label),
                            onSecondaryButtonClick = {
                                viewModel.navigateToHome()
                            },
                            isAccentButtonVisible = false
                        )
                    }
                }
                is DeviceBatteryOptimizer.State.ReadingData -> {
                    operationProgressView.isVisible = false
                }
            }
        }
    }
}