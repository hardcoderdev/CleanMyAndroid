package runtime.exception.cleanmyandroid.base.customViews

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import runtime.exception.cleanmyandroid.R

class SliderView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var sliderBackground: View? = null
    private var shimmerSeekbarLayout: ShimmerFrameLayout? = null
    private var shimmerIconLayout: ShimmerFrameLayout? = null
    private var sliderSeekbar: SeekBar? = null
    private var sliderIcon: ImageView? = null
    private var completedTextView: TextView? = null

    init {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.slider_view, this, true)
        sliderBackground = findViewById(R.id.sliderBackground)
        shimmerSeekbarLayout = findViewById(R.id.shimmerSeekbarLayout)
        shimmerIconLayout = findViewById(R.id.shimmerIconLayout)
        sliderSeekbar = findViewById(R.id.slider_seekbar)
        sliderIcon = findViewById(R.id.sliderIcon)
        completedTextView = findViewById(R.id.completedTextView)
    }

    var state: State? = null
        @SuppressLint("ClickableViewAccessibility")
        set(value) {
            field = value
            sliderBackground?.setBackgroundResource(value?.trackBackground ?: 0)
            when (value) {
                is State.Available -> {
                    shimmerSeekbarLayout?.startShimmer()
                    shimmerIconLayout?.startShimmer()
                    sliderSeekbar?.thumb?.alpha = 255
                    sliderSeekbar?.isClickable = true
                    sliderSeekbar?.isEnabled = true
                    sliderIcon?.setImageResource(value.iconResource)

                    val defaultOffset = sliderSeekbar?.thumbOffset
                    val thumbWidth = sliderSeekbar?.thumb!!.intrinsicWidth
                    val thumbDrawable = ResourcesCompat.getDrawable(resources, value.thumbResource, null)
                    sliderSeekbar?.thumb = thumbDrawable
                    if (defaultOffset != null) {
                        sliderSeekbar?.thumbOffset = defaultOffset
                    }
                    sliderSeekbar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                        override fun onStartTrackingTouch(seekBar: SeekBar) {}
                        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) {
                            sliderIcon?.alpha = 1f - progress * 0.02f
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar) {
                            if (seekBar.progress < 100) {
                                val anim = ObjectAnimator.ofInt(seekBar, "progress", 0)
                                anim.interpolator = AccelerateDecelerateInterpolator()
                                anim.duration =
                                    resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                                anim.start()
                            } else {
                                value.onSlideListener.invoke()
                            }
                        }
                    })

                    sliderSeekbar?.setOnTouchListener { _, motionEvent ->
                        var isInvalidMove = false
                        return@setOnTouchListener when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isInvalidMove = motionEvent.x > thumbWidth
                                isInvalidMove
                            }
                            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> isInvalidMove
                            else -> false
                        }
                    }
                }
                is State.Completed -> {
                    shimmerSeekbarLayout?.stopShimmer()
                    shimmerIconLayout?.stopShimmer()
                    sliderSeekbar?.setOnSeekBarChangeListener(null)
                    sliderSeekbar?.setOnTouchListener(null)
                    completedTextView?.isVisible = true
                    sliderIcon?.isVisible = false
                    sliderSeekbar?.isVisible = false
                    val drawableToLock = ResourcesCompat.getDrawable(context.resources, value.iconResource, null)
                    val completedTextColor = ResourcesCompat.getColor(context.resources, value.textColor, null)
                    completedTextView?.apply {
                        text = context.getString(value.title)
                        setTextColor(completedTextColor)
                        compoundDrawablePadding = value.drawablePadding
                        setCompoundDrawablesWithIntrinsicBounds(drawableToLock, null, null, null)
                    }
                }
                is State.Locked -> {
                    shimmerSeekbarLayout?.stopShimmer()
                    shimmerIconLayout?.stopShimmer()
                    val defaultOffset = sliderSeekbar?.thumbOffset
                    val thumbDrawable = ResourcesCompat.getDrawable(resources, value.thumbResource, null)
                    sliderSeekbar?.thumb = thumbDrawable
                    if (defaultOffset != null) {
                        sliderSeekbar?.thumbOffset = defaultOffset
                    }

                    sliderIcon?.setImageResource(value.iconResource)
                    sliderSeekbar?.setOnSeekBarChangeListener(null)
                    sliderSeekbar?.setOnTouchListener(null)
                    sliderSeekbar?.apply {
                        thumb.alpha = 127
                        isClickable = false
                        isEnabled = false
                    }
                }
            }
        }

    sealed class State {
        abstract val trackBackground: Int

        data class Available(
            @DrawableRes val thumbResource: Int,
            @DrawableRes val iconResource: Int,
            val onSlideListener: () -> Unit,
            override val trackBackground: Int
        ) : State()

        data class Locked(
            @DrawableRes val thumbResource: Int,
            @DrawableRes val iconResource: Int,
            override val trackBackground: Int
        ) : State()

        data class Completed(
            val drawablePadding: Int,
            @ColorRes internal val textColor: Int,
            override val trackBackground: Int,
            @StringRes val title: Int,
            @DrawableRes val iconResource: Int
        ) : State()
    }
}