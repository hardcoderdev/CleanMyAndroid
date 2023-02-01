package runtime.exception.cleanmyandroid.base.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarMenuView
import q.rorbin.badgeview.QBadgeView
import runtime.exception.cleanmyandroid.R

class ExtendedBottomBar(context: Context, attributeSet: AttributeSet) :
    BottomNavigationView(context, attributeSet) {

    private val badgeMap = hashMapOf<Int, QBadgeView>()
    private val lockBadge = ResourcesCompat.getDrawable(context.resources, R.drawable.lock, null)

    override fun createNavigationBarMenuView(context: Context): NavigationBarMenuView {
        return ExtendedBottomBarMenuView(context)
    }

    @SuppressLint("RestrictedApi")
    fun setItemEnabled(isEnabled: Boolean, @IdRes id: Int) {
        val menuItemView = (menuView as ExtendedBottomBarMenuView).findItemView(id)
        menu.findItem(id).isEnabled = isEnabled
        if (isEnabled) {
            badgeMap[id]?.hide(true)
            badgeMap.remove(id)
        } else {
            if (!badgeMap.containsKey(id)) {
                badgeMap[id] = QBadgeView(context).also {
                    it.bindTarget(menuItemView)
                    it.badgeBackground = lockBadge
                    it.badgeBackgroundColor = Color.YELLOW
                    it.setGravityOffset(6f, 8f, true)
                    it.setBadgeTextSize(14f, true)
                    it.badgeText = " "
                }
            }
        }
    }

    override fun getMaxItemCount(): Int {
        return MAX_ITEM_COUNT
    }

    companion object {
        const val MAX_ITEM_COUNT = 6
    }
}

@SuppressLint("RestrictedApi")
class ExtendedBottomBarMenuView(context: Context) : BottomNavigationMenuView(context) {

    private var inactiveItemMaxWidth = 0
    private var inactiveItemMinWidth = 0
    private var activeItemMaxWidth = 0
    private var activeItemMinWidth = 0

    private var tempChildWidths: IntArray

    init {
        val params = FrameLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER
        layoutParams = params
        val res = resources
        inactiveItemMaxWidth =
            res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_max_width)
        inactiveItemMinWidth =
            res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_min_width)
        activeItemMaxWidth =
            res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_max_width)
        activeItemMinWidth =
            res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_min_width)
        tempChildWidths = IntArray(ExtendedBottomBar.MAX_ITEM_COUNT)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val menu = menu
        val width = MeasureSpec.getSize(widthMeasureSpec)
        // Use visible item count to calculate widths
        val visibleCount = menu!!.visibleItems.size
        // Use total item counts to measure children
        val totalCount = childCount
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY)
        if (isShifting(labelVisibilityMode, visibleCount)
            && isItemHorizontalTranslationEnabled
        ) {
            val activeChild = getChildAt(selectedItemPosition)
            var activeItemWidth = activeItemMinWidth
            if (activeChild.visibility != GONE) {
                // Do an AT_MOST measure pass on the active child to get its desired width, and resize the
                // active child view based on that width
                activeChild.measure(
                    MeasureSpec.makeMeasureSpec(activeItemMaxWidth, MeasureSpec.AT_MOST), heightSpec
                )
                activeItemWidth = Math.max(activeItemWidth, activeChild.measuredWidth)
            }
            val inactiveCount = visibleCount - if (activeChild.visibility != GONE) 1 else 0
            val activeMaxAvailable = width - inactiveCount * inactiveItemMinWidth
            val activeWidth =
                activeMaxAvailable.coerceAtMost(Math.min(activeItemWidth, activeItemMaxWidth))
            val inactiveMaxAvailable =
                (width - activeWidth) / if (inactiveCount == 0) 1 else inactiveCount
            val inactiveWidth = inactiveMaxAvailable.coerceAtMost(inactiveItemMaxWidth)
            var extra = width - activeWidth - inactiveWidth * inactiveCount
            for (i in 0 until totalCount) {
                if (getChildAt(i).visibility != GONE) {
                    tempChildWidths[i] =
                        if (i == selectedItemPosition) activeWidth else inactiveWidth
                    // Account for integer division which sometimes leaves some extra pixel spaces.
                    // e.g. If the nav was 10px wide, and 3 children were measured to be 3px-3px-3px, there
                    // would be a 1px gap somewhere, which this fills in.
                    if (extra > 0) {
                        tempChildWidths[i]++
                        extra--
                    }
                } else {
                    tempChildWidths[i] = 0
                }
            }
        } else {
            val maxAvailable = width / if (visibleCount == 0) 1 else visibleCount
            val childWidth = Math.min(maxAvailable, activeItemMaxWidth)
            var extra = width - childWidth * visibleCount
            for (i in 0 until totalCount) {
                if (getChildAt(i).visibility != GONE) {
                    tempChildWidths[i] = childWidth
                    if (extra > 0) {
                        tempChildWidths[i]++
                        extra--
                    }
                } else {
                    tempChildWidths[i] = 0
                }
            }
        }
        var totalWidth = 0
        for (i in 0 until totalCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            child.measure(
                MeasureSpec.makeMeasureSpec(tempChildWidths[i], MeasureSpec.EXACTLY), heightSpec
            )
            val params = child.layoutParams
            params.width = child.measuredWidth
            totalWidth += child.measuredWidth
        }
        setMeasuredDimension(
            resolveSizeAndState(
                totalWidth, MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY), 0
            ),
            resolveSizeAndState(parentHeight, heightMeasureSpec, 0)
        )
    }


}