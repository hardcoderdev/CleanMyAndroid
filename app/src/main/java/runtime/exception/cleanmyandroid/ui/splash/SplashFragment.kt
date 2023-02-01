package runtime.exception.cleanmyandroid.ui.splash

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.extensions.booleanPreference
import runtime.exception.cleanmyandroid.common.preferences.SharedPreferencesHolder
import runtime.exception.cleanmyandroid.databinding.SplashFragmentBinding
import runtime.exception.cleanmyandroid.ui.BaseApplication

class SplashFragment : Fragment(R.layout.splash_fragment),
    BaseApplication.OnShowAdCompleteListener, SharedPreferencesHolder {

    private val binding by viewBinding<SplashFragmentBinding>()
    private val viewModel by viewModel<SplashViewModel>()
    private var isFirstLaunch by booleanPreference(defValue = true, name = IS_FIRST_LAUNCH_KEY)
    override val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences(
            getString(R.string.shared_prefs_file),
            Context.MODE_PRIVATE
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadProgressBar()
        subscribeToObservers()
    }

    private fun subscribeToObservers() = with(viewModel) {
        progress.onEach {
            binding.progressBar.progress = it
        }.launchIn(lifecycleScope)

        events.onEach { event ->
            when (event) {
                SplashViewModel.SplashEvents.ShowOnOpenAppAd -> {
                    (requireActivity().application as BaseApplication).showAdIfAvailable(
                        requireActivity(),
                        this@SplashFragment
                    )
                }
                SplashViewModel.SplashEvents.NavigateToWelcome -> {
                    if (isFirstLaunch) {
                        findNavController().navigate(R.id.welcomeFragment)
                    } else {
                        findNavController().navigate(R.id.navigationContainerFragment)
                    }
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onShowAdComplete() {
        viewModel.navigateToWelcome()
    }

    companion object {
        private const val IS_FIRST_LAUNCH_KEY = "is_first_launch"
    }
}