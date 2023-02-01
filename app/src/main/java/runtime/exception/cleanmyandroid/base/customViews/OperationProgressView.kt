package runtime.exception.cleanmyandroid.base.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.google.android.gms.ads.*
import com.google.android.material.button.MaterialButton
import net.vrgsoft.arcprogress.ArcProgressBar
import runtime.exception.cleanmyandroid.R

class OperationProgressView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private var finishTitleTextView: TextView? = null
    private var finishAccentButton: MaterialButton? = null
    private var finishSecondaryButton: MaterialButton? = null
    private var progressArcProgressBar: ArcProgressBar? = null
    private var progressPercentTextView: TextView? = null
    private var progressWhatWeAreDoingTextView: TextView? = null
    private var finishLayout: ConstraintLayout? = null
    private var progressConstraintLayout: ConstraintLayout? = null
    private var adView: AdView? = null

    init {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_operation_progres, this, true)
        finishTitleTextView = findViewById(R.id.finishTitleTextView)
        finishAccentButton = findViewById(R.id.finishAccentButton)
        progressArcProgressBar = findViewById(R.id.progressArcProgressBar)
        progressPercentTextView = findViewById(R.id.progressPercentTextView)
        progressWhatWeAreDoingTextView = findViewById(R.id.progressWhatWeAreDoingTextView)
        finishLayout = findViewById(R.id.finishLayout)
        progressConstraintLayout = findViewById(R.id.progress_constraint_layout)
        adView = findViewById(R.id.adView)
    }

    var state: State? = null
        set(value) {
            field = value
            finishLayout?.isVisible = value is State.Finished
            progressConstraintLayout?.isVisible = value is State.Running
            when (value) {
                is State.Finished -> {
                    finishTitleTextView?.text = value.title

                    finishAccentButton?.text = value.accentButtonText
                    finishAccentButton?.icon = ResourcesCompat.getDrawable(
                        resources,
                        value.accentButtonIcon,
                        null
                    )

                    finishSecondaryButton?.text = value.secondaryButtonText

                    finishAccentButton?.setOnClickListener {
                        value.onAccentButtonClick.invoke()
                    }

                    finishSecondaryButton?.setOnClickListener {
                        value.onSecondaryButtonClick.invoke()
                    }

                    finishAccentButton?.isVisible = value.isAccentButtonVisible
                }
                is State.Running -> {
                    progressArcProgressBar?.progress = value.progress
                    progressPercentTextView?.text = context.getString(R.string.percentage_format, value.progress)
                    progressWhatWeAreDoingTextView?.text = value.operationTitle
                    adView?.loadAd(AdRequest.Builder().build())
                }
            }
        }

    sealed class State {
        data class Finished(
            val title: String,
            val accentButtonText: String,
            @DrawableRes val accentButtonIcon: Int,
            val secondaryButtonText: String,
            val onAccentButtonClick: () -> Unit = {},
            val onSecondaryButtonClick: () -> Unit,
            val isAccentButtonVisible: Boolean = true,
        ) : State()

        data class Running(
            val progress: Int,
            val operationTitle: String
        ) : State()
    }
}