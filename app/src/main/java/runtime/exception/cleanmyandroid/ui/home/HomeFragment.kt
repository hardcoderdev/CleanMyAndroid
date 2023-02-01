package runtime.exception.cleanmyandroid.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import kotlinx.coroutines.flow.filterNotNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.extensions.booleanPreference
import runtime.exception.cleanmyandroid.base.extensions.launchAndCollectIn
import runtime.exception.cleanmyandroid.base.extensions.setColorFilterByColor
import runtime.exception.cleanmyandroid.common.adapters.diffCallback
import runtime.exception.cleanmyandroid.common.preferences.SharedPreferencesHolder
import runtime.exception.cleanmyandroid.databinding.HomeFragmentBinding
import runtime.exception.cleanmyandroid.ui.home.HomeViewModel.*
import runtime.exception.cleanmyandroid.ui.home.models.itemHomeFeatureDelegate

class HomeFragment : Fragment(R.layout.home_fragment), SharedPreferencesHolder {

    private val binding by viewBinding<HomeFragmentBinding>()
    private val viewModel by viewModel<HomeViewModel>()
    private var isFirstLaunch by booleanPreference(defValue = true, name = IS_FIRST_LAUNCH_KEY)
    private val sliders = AsyncListDifferDelegationAdapter(diffCallback, itemHomeFeatureDelegate() { featureItem ->
        when (featureItem) {
            is FeatureItem.Battery -> viewModel.navigateToBatteryOptimization()
            is FeatureItem.Boost -> viewModel.navigateToDeviceBoost()
            is FeatureItem.Memory -> viewModel.navigateToMemoryClean()
            is FeatureItem.Threats -> viewModel.navigateToThreatProtection()
        }
    })

    override val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences(
            getString(R.string.shared_prefs_file),
            Context.MODE_PRIVATE
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isFirstLaunch) isFirstLaunch = false
        setUpRecyclerView()
        subscribeToObservers()
    }

    private fun setUpRecyclerView() {
        binding.sliderRecyclerView.adapter = sliders
    }

    private fun subscribeToObservers() = with(binding) {
        viewModel.events.launchAndCollectIn(viewLifecycleOwner) { event ->
            when (event) {
                HomeEvents.NavigateToBatteryOptimization -> {
                    findNavController().navigate(
                        R.id.optimizeBatteryFragment, null, NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, true)
                            .build()
                    )
                }
                HomeEvents.NavigateToDeviceBoost -> {
                    findNavController().navigate(
                        R.id.deviceBoostFragment, null, NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, true)
                            .build()
                    )
                }
                HomeEvents.NavigateToMemoryClean -> {
                    findNavController().navigate(
                        R.id.smartCleaningFragment, null, NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, true)
                            .build()
                    )
                }
                HomeEvents.NavigateToThreatProtection -> {
                    findNavController().navigate(
                        R.id.threatProtectionFragment, null, NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, true)
                            .build()
                    )
                }
            }
        }

        viewModel.state.filterNotNull().launchAndCollectIn(viewLifecycleOwner) {
            deviceHealthLevelProgressBar.progress = it.deviceProtectionLevelProgress

            deviceHealthLevelTextView.text = when (it.deviceProtectionLevel) {
                DeviceProtectionLevel.MINIMUM -> getString(R.string.minimum_level_title)
                DeviceProtectionLevel.LOW -> getString(R.string.low_level_title)
                DeviceProtectionLevel.AVERAGE -> getString(R.string.average_level_title)
                DeviceProtectionLevel.ABOVE_AVERAGE -> getString(R.string.above_average_level_title)
                DeviceProtectionLevel.MAXIMUM -> getString(R.string.maximum_level_title)
            }

            descriptionTextView.text = when (it.deviceProtectionLevel) {
                DeviceProtectionLevel.MINIMUM -> getString(R.string.minimum_level_description)
                DeviceProtectionLevel.LOW -> getString(R.string.low_level_description)
                DeviceProtectionLevel.AVERAGE -> getString(R.string.average_level_description)
                DeviceProtectionLevel.ABOVE_AVERAGE -> getString(R.string.above_average_level_description)
                DeviceProtectionLevel.MAXIMUM -> getString(R.string.maximum_level_description)
            }

            val progressDrawable = deviceHealthLevelProgressBar.progressDrawable.mutate()
            val color = when (it.deviceProtectionLevel) {
                DeviceProtectionLevel.MINIMUM -> ResourcesCompat.getColor(resources, R.color.minimum_level, null)
                DeviceProtectionLevel.LOW -> ResourcesCompat.getColor(resources, R.color.low_level, null)
                DeviceProtectionLevel.AVERAGE -> ResourcesCompat.getColor(resources, R.color.average_level, null)
                DeviceProtectionLevel.ABOVE_AVERAGE -> ResourcesCompat.getColor(resources, R.color.above_average_level, null)
                DeviceProtectionLevel.MAXIMUM -> ResourcesCompat.getColor(resources, R.color.maximum_level, null)
            }

            progressDrawable.setColorFilterByColor(color)
            deviceHealthLevelProgressBar.progressDrawable = progressDrawable

            deviceHealthLevelTextView.setTextColor(color)

            sliders.items = it.featureItems
        }
    }

    companion object {
        private const val IS_FIRST_LAUNCH_KEY = "is_first_launch"
    }
}