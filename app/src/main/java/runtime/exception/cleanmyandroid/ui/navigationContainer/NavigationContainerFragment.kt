package runtime.exception.cleanmyandroid.ui.navigationContainer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.extensions.launchAndCollectIn
import runtime.exception.cleanmyandroid.databinding.NavigationContainerFragmentBinding

class NavigationContainerFragment : Fragment(R.layout.navigation_container_fragment) {

    private val viewModel by viewModel<NavigationContainerViewModel>()
    private val binding by viewBinding<NavigationContainerFragmentBinding>()
    private val navHostFragment by lazy { childFragmentManager.findFragmentById(R.id.appFragmentContainerView) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    private fun subscribeToObservers() = with(binding) {
        viewModel.state.launchAndCollectIn(viewLifecycleOwner) {
            bottomNavigationView.setItemEnabled(isEnabled = it.isSmartCleaningEnabled, R.id.smartCleaningFragment)
            bottomNavigationView.setItemEnabled(isEnabled = it.isThreatProtectionEnabled, R.id.threatProtectionFragment)
            bottomNavigationView.setItemEnabled(isEnabled = it.isBatteryOptimizedEnabled, R.id.optimizeBatteryFragment)
        }
    }
}