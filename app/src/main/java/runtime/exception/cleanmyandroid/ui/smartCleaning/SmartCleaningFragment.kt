package runtime.exception.cleanmyandroid.ui.smartCleaning

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import runtime.exception.cleanmyandroid.R
import runtime.exception.cleanmyandroid.base.customViews.OperationProgressView
import runtime.exception.cleanmyandroid.base.customViews.SliderView
import runtime.exception.cleanmyandroid.base.extensions.booleanPreference
import runtime.exception.cleanmyandroid.base.extensions.launchAndCollectIn
import runtime.exception.cleanmyandroid.common.BlurBuilder
import runtime.exception.cleanmyandroid.common.adapters.smartDiffCallback
import runtime.exception.cleanmyandroid.common.features.boostFeatures.DeviceCleaner
import runtime.exception.cleanmyandroid.common.formatters.MegabytesFormatter
import runtime.exception.cleanmyandroid.common.managers.InterstitialAdManager
import runtime.exception.cleanmyandroid.common.preferences.SharedPreferencesHolder
import runtime.exception.cleanmyandroid.databinding.SmartCleaningFragmentBinding
import runtime.exception.cleanmyandroid.ui.smartCleaning.dialogs.SmartCleanGuideDialog
import runtime.exception.cleanmyandroid.ui.smartCleaning.models.itemAppDelegate
import java.util.concurrent.TimeUnit

class SmartCleaningFragment : Fragment(R.layout.smart_cleaning_fragment), SharedPreferencesHolder {

    private val interstitialAdManager by inject<InterstitialAdManager>()
    private val megabytesFormatter by inject<MegabytesFormatter>()
    private var isTutorialAlreadyShowed by booleanPreference(defValue = false, name = IS_FIRST_SMART_CLEAN)
    private val binding by viewBinding<SmartCleaningFragmentBinding>()
    private val viewModel by viewModel<SmartCleaningViewModel>()
    private val installedAppsAdapter = AsyncListDifferDelegationAdapter(smartDiffCallback, itemAppDelegate {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:${it.packageName}")
        deleteAppCallback.launch(intent)
    })

    override val sharedPreferences: SharedPreferences by lazy {
        requireActivity().getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE)
    }

    private val deleteAppCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.loadInstalledApps()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition(100, TimeUnit.MILLISECONDS)
        subscribeToObservers()
        setUpViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.interruptSmartCleaning()
    }

    private fun setUpViews() = with(binding) {
        appManagerBottomSheet.appsRecyclerView.adapter = installedAppsAdapter
    }

    private fun subscribeToObservers() = with(binding) {
        viewModel.events.launchAndCollectIn(viewLifecycleOwner) { event ->
            when (event) {
                is SmartCleaningViewModel.SmartCleaningEvents.NavigateToHome -> {
                    findNavController().navigate(R.id.homeFragment)
                }
                is SmartCleaningViewModel.SmartCleaningEvents.NavigateToThreatProtection -> {
                    findNavController().navigate(
                        R.id.threatProtectionFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .build()
                    )
                }
            }
        }

        viewModel.state.filterNotNull().launchAndCollectIn(viewLifecycleOwner) { state ->
            when (val cleanerState = state.deviceCleanerState) {
                is DeviceCleaner.State.Cleaned -> {
                    cacheMemoryTextView.text = megabytesFormatter.format(0)
                    temporaryFilesTextView.text = megabytesFormatter.format(0)
                    residualFilesTextView.text = megabytesFormatter.format(0)
                    systemJunkTextView.text = megabytesFormatter.format(0)

                    cacheProgressBar.progress = 0
                    temporaryFilesProgressBar.progress = 0
                    residualFilesProgressBar.progress = 0
                    systemJunkProgressBar.progress = 0

                    cacheProgressBar.max = 0
                    temporaryFilesProgressBar.max = 0
                    residualFilesProgressBar.max = 0
                    systemJunkProgressBar.max = 0

                    val color = ResourcesCompat.getColor(resources, R.color.green, null)

                    cacheMemoryTextView.setTextColor(color)
                    temporaryFilesTextView.setTextColor(color)
                    residualFilesTextView.setTextColor(color)
                    systemJunkTextView.setTextColor(color)

                    privacyLabelTextView.isVisible = false
                    upperPrivacyLabelTextView.isVisible = true

                    cleanSlideToUnlockView.state = SliderView.State.Completed(
                        drawablePadding = 24,
                        iconResource = R.drawable.ic_check_white,
                        trackBackground = R.drawable.button_background,
                        title = R.string.no_trash_label,
                        textColor = R.color.white
                    )

                    if (state.installedAppsState.apps.isEmpty()) {
                        appManagerBottomSheet.appsRecyclerView.isVisible = false
                        appManagerBottomSheet.appsProgressBar.isVisible = true
                    } else {
                        installedAppsAdapter.items = state.installedAppsState.apps
                        appManagerBottomSheet.appsRecyclerView.isVisible = true
                        appManagerBottomSheet.appsProgressBar.isVisible = false
                    }
                }
                is DeviceCleaner.State.Dirty -> {
                    operationProgressView.isVisible = false

                    cacheMemoryTextView.text = megabytesFormatter.format(cleanerState.cacheFileMegabytes)
                    temporaryFilesTextView.text = megabytesFormatter.format(cleanerState.temporaryFileMegabytes)
                    residualFilesTextView.text = megabytesFormatter.format(cleanerState.residualFileMegabytes)
                    systemJunkTextView.text = megabytesFormatter.format(cleanerState.systemJunkMegabytes)

                    cacheProgressBar.progress = cleanerState.cacheFileMegabytes
                    temporaryFilesProgressBar.progress = cleanerState.temporaryFileMegabytes
                    residualFilesProgressBar.progress = cleanerState.residualFileMegabytes
                    systemJunkProgressBar.progress = cleanerState.systemJunkMegabytes

                    cacheProgressBar.max = 150
                    temporaryFilesProgressBar.max = 150
                    residualFilesProgressBar.max = 100
                    systemJunkProgressBar.max = 100

                    val color = ResourcesCompat.getColor(resources, R.color.another_red, null)

                    cacheMemoryTextView.setTextColor(color)
                    temporaryFilesTextView.setTextColor(color)
                    residualFilesTextView.setTextColor(color)
                    systemJunkTextView.setTextColor(color)

                    privacyLabelTextView.isVisible = true
                    upperPrivacyLabelTextView.isVisible = false

                    cleanSlideToUnlockView.state = SliderView.State.Available(
                        thumbResource = R.drawable.thumb_clean,
                        iconResource = R.drawable.ic_slide_green,
                        trackBackground = R.drawable.slider_start_background,
                        onSlideListener = {
                            viewModel.cleanJunk()
                        }
                    )

                    if (state.installedAppsState.apps.isEmpty()) {
                        appManagerBottomSheet.appsRecyclerView.isVisible = false
                        appManagerBottomSheet.appsProgressBar.isVisible = true
                    } else {
                        installedAppsAdapter.items = state.installedAppsState.apps
                        appManagerBottomSheet.appsRecyclerView.isVisible = true
                        appManagerBottomSheet.appsProgressBar.isVisible = false
                    }

                    if (!isTutorialAlreadyShowed) {
                        delay(500L)

                        operationProgressView.isVisible = false
                        val rootView = requireActivity().window.decorView.rootView
                        rootView.isDrawingCacheEnabled = true
                        val bitmap = Bitmap.createBitmap(rootView.drawingCache)
                        rootView.isDrawingCacheEnabled = false

                        val blurredBitmap = BlurBuilder.blur(requireContext(), bitmap)
                        val guideDialog = SmartCleanGuideDialog()
                        guideDialog.arguments = bundleOf("bitmap" to blurredBitmap)
                        guideDialog.show(requireActivity().supportFragmentManager, "TAG")
                        isTutorialAlreadyShowed = true
                    }
                }
                is DeviceCleaner.State.Cleaning -> {
                    if (cleanerState.currentPercent == 99) {
                        interstitialAdManager.show(requireActivity())
                    }
                    operationProgressView.isVisible = true
                    operationProgressView.state = if (cleanerState.currentPercent < 100) {
                        OperationProgressView.State.Running(
                            progress = cleanerState.currentPercent,
                            operationTitle = when (cleanerState.operationType) {
                                DeviceCleaner.OperationType.CLEANING_CACHE -> resources.getStringArray(R.array.smart_clean)[0]
                                DeviceCleaner.OperationType.REMOVE_TEMP_FILES -> resources.getStringArray(R.array.smart_clean)[1]
                                DeviceCleaner.OperationType.OPTIMIZING_SPACE -> resources.getStringArray(R.array.smart_clean)[2]
                            }
                        )
                    } else {
                        OperationProgressView.State.Finished(
                            title = getString(R.string.smart_clean_completed),
                            accentButtonText = getString(R.string.smart_clean_finish),
                            accentButtonIcon = R.drawable.ic_threat_protection,
                            secondaryButtonText = getString(R.string.ok_label),
                            onAccentButtonClick = {
                                viewModel.navigateToThreatProtection()
                            },
                            onSecondaryButtonClick = {
                                viewModel.navigateToHome()
                            }
                        )
                    }
                }
                is DeviceCleaner.State.ReadingData -> {
                    operationProgressView.isVisible = false
                }
            }
        }
    }

    companion object {
        private const val IS_FIRST_SMART_CLEAN = "is_first_smart_clean_key"
    }
}