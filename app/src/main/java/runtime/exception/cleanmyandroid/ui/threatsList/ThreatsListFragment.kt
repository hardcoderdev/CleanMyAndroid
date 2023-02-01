package runtime.exception.cleanmyandroid.ui.threatsList

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.extensions.launchAndCollectIn
import runtime.exception.cleanmyandroid.common.adapters.threatsDiffCallback
import runtime.exception.cleanmyandroid.databinding.ThreatsListFragmentBinding
import runtime.exception.cleanmyandroid.ui.threatsList.models.harmfullyAppsDelegate

class ThreatsListFragment : Fragment(R.layout.threats_list_fragment) {

    private val binding by viewBinding<ThreatsListFragmentBinding>()
    private val viewModel by viewModel<ThreatsListViewModel>()
    private val adapter by lazy {
        AsyncListDifferDelegationAdapter(threatsDiffCallback, harmfullyAppsDelegate(requireActivity().packageManager))
    }

    private val deleteAppCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* no-op */ }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClicks()
        setUpRecyclerView()
        subscribeToObservers()
    }

    private fun setUpClicks() = with(binding) {
        unInstallAppsButton.setOnClickListener {
            viewModel.state.value.threatApps.forEachIndexed { index, itemThreatApp ->
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:${itemThreatApp.packageName}")
                deleteAppCallback.launch(intent)
                if (index == viewModel.state.value.threatApps.size) {
                    viewModel.navigateToHome()
                }
            }
        }

        ignoreButton.setOnClickListener {
            viewModel.navigateToHome()
        }
    }

    private fun setUpRecyclerView() = with(binding) {
        threatsRecyclerView.adapter = adapter
    }

    private fun subscribeToObservers() = with(binding) {
        viewModel.events.launchAndCollectIn(viewLifecycleOwner) { event ->
            when (event) {
                is ThreatsListViewModel.ThreatsEvents.NavigateToHome -> {
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }

        viewModel.state.launchAndCollectIn(viewLifecycleOwner) {
            unInstallAppsButton.text =
                getString(R.string.uninstall_format, it.threatApps.size)
            threatsCountTextView.text =
                getString(R.string.founded_threats_format, it.threatApps.size)
            adapter.items = it.threatApps
        }
    }
}