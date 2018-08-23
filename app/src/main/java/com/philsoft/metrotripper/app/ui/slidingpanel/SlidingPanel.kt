package com.philsoft.metrotripper.app.ui.slidingpanel

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.support.v4.math.MathUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.philsoft.metrotripper.utils.ui.Ui

/**
 * View than can slide up from the bottom of the screen to overlay the main content.   This view must
 * contain 2 children.   The first child is the main content and the second child is the the panel
 * that will slide up
 */
open class SlidingPanel @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {


    /**
     * Amount of drag velocity required before the panel will start to move.  Measured in pixels / second.
     * Default value is 750
     */
    var dragVelocityThreshold = 750f

    /**
     * Amount in px the sliding panel should peek up from the bottom
     */
    var bottomOffset = 0

    /**
     * Distance from top of the screen that the panel should slide up to
     */
    var topOffset = 0

    /**
     * Views will slide up the panel when swiped
     */
    val swipableViews: ArrayList<View> by lazy { arrayListOf(getChildAt(1)) }

    /**
     * Listener that is called when the panel is being dragged or animated.   `scrollOffset` is a
     * value between 0 and 1, where 1 indicates the panel is fully expanded, and 0 is fully collapsed
     */
    var scrollListener: (scrollOffset: Float) -> Unit = {}

    /**
     * Called when the panel changes state
     */
    var panelStateChangeListener: (panelState: PanelState) -> Unit = {}


    /**
     * The sliding up view
     */
    lateinit var panelView: View
        private set

    private val screenHeight by lazy { Ui.getCurrentScreenHeight(context).toFloat() }
    private val panelViewCollapsedY get() = screenHeight - bottomOffset
    private val panelViewExpandedY by lazy { topOffset.toFloat() }
    private val visibleRect = Rect()
    private val dragDetector = VerticalDragDetector(object : VerticalDragDetector.DragListener {
        override fun onDrag(distanceY: Float) {
            handleDrag(distanceY)
        }

        override fun onDragStopped(velocity: Float) {
            handleDragStopped(velocity)
        }
    }, dragVelocityThreshold)

    private var panelState: PanelState = PanelState.COLLAPSED
    private var expandAnimator: ObjectAnimator? = null
    private var collapseAnimator: ObjectAnimator? = null

    enum class PanelState {
        COLLAPSED,
        EXPANDED,
        DRAGGING,
        HIDDEN
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        panelView = getChildAt(1)
        setupPanelView()
    }

    /**
     * Sets up a click listener on the panel.  Clicking on the panel will cause it to expand or
     * collapse
     */
    private fun setupPanelView() {
        panelView.setOnClickListener {
            if (panelState == PanelState.EXPANDED) {
                collapsePanel()
            } else if (panelState == PanelState.COLLAPSED) {
                expandPanel()
            }
        }
    }

    /**
     * Determines whether or not to handle this touch event in `onTouchEvent`.  If the motion event
     * occurred over the specified `scrollableView`, then we let that view handle the event
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled) {
            return super.onInterceptTouchEvent(ev)
        }

        val isEventOnSwipableView = areAnyViewsUnder(swipableViews, ev.rawX.toInt(), ev.rawY.toInt())
        if (!isEventOnSwipableView) {
            return super.onInterceptTouchEvent(ev)
        }

        dragDetector.trackEvent(ev)
        if (dragDetector.isDragging) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        dragDetector.trackEvent(event)
        return false
    }

    /**
     * Called when a drag event occurs in the specified distance.   A negative distance indicates
     * a drag up, and a positive distance indicates a drag down
     */
    open fun handleDrag(distanceY: Float) {
        when {
            panelView.y + distanceY > panelViewCollapsedY -> panelView.y = panelViewCollapsedY
            panelView.y + distanceY < panelViewExpandedY -> panelView.y = panelViewExpandedY
            else -> panelView.y += distanceY.toInt()
        }
        notifyScrollListener()
        notifyPanelState(PanelState.DRAGGING)
    }

    /**
     * Called when the user stops dragging, i.e. lifts up their finger
     */
    private fun handleDragStopped(velocity: Float) {
        when {
            velocity < -dragVelocityThreshold -> expandPanel()
            velocity > dragVelocityThreshold -> collapsePanel()
            else -> animateToClosest()
        }
    }

    /**
     * Called to force the panel to collapse, expand, or hide.  A value of `PanelState.DRAGGING` has
     * no effect
     */
    fun setPanelState(panelState: PanelState) {
        when (panelState) {
            PanelState.COLLAPSED -> collapsePanel()
            PanelState.EXPANDED -> expandPanel()
            PanelState.HIDDEN -> hidePanel()
        }
    }

    private fun collapsePanel() {
        cancelAnimations()
        panelView.visibility = View.VISIBLE
        isEnabled = true
        collapseAnimator = ObjectAnimator.ofFloat(panelView, "y", panelView.y, panelViewCollapsedY).apply {
            interpolator = DecelerateInterpolator()
            duration = 250
            addStartListener { notifyPanelState(PanelState.DRAGGING) }
            addUpdateListener { notifyScrollListener() }
            addEndListener { notifyPanelState(PanelState.COLLAPSED) }
            start()
        }
    }

    private fun expandPanel() {
        cancelAnimations()
        panelView.visibility = View.VISIBLE
        isEnabled = true
        expandAnimator = ObjectAnimator.ofFloat(panelView, "y", panelView.y, panelViewExpandedY).apply {
            interpolator = DecelerateInterpolator()
            duration = 250
            addStartListener { notifyPanelState(PanelState.DRAGGING) }
            addUpdateListener { notifyScrollListener() }
            addEndListener { notifyPanelState(PanelState.EXPANDED) }
            start()
        }
    }

    private fun hidePanel() {
        cancelAnimations()
        panelView.visibility = View.GONE
        panelView.y = screenHeight
        isEnabled = false
    }

    private fun areAnyViewsUnder(views: List<View>, x: Int, y: Int): Boolean {
        views.forEach {
            if (isViewUnder(it, x, y)) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if a view exists at the specified coordinates
     */
    private fun isViewUnder(view: View?, x: Int, y: Int): Boolean {
        return if (view == null) {
            false
        } else {
            view.getGlobalVisibleRect(visibleRect)
            visibleRect.run { x in left..(right - 1) && y in top..(bottom - 1) }
        }
    }

    /**
     * Expands the panel if its current y position is halfway above the screen, or collapses if not
     */
    private fun animateToClosest() {
        if (panelView.y < screenHeight / 2) {
            expandPanel()
        } else {
            collapsePanel()
        }
    }

    private fun cancelAnimations() {
        collapseAnimator?.cancel()
        expandAnimator?.cancel()
    }

    /**
     * Calls the panel state listener if the panel state has changed
     */
    private fun notifyPanelState(state: PanelState) {
        if (state != panelState) {
            panelState = state
            panelStateChangeListener(state)
        }
    }

    /**
     * Notifies that a drag has occurred.   This is called multiple times as the view is dragging
     * or animating
     */
    private fun notifyScrollListener() {
        val scrollOffset = 1 - (panelView.y / panelViewCollapsedY)
        val clampedScrollOffset = MathUtils.clamp(scrollOffset, 0f, 1f)
        scrollListener(clampedScrollOffset)
    }

    //Extension functions
    private fun ObjectAnimator.addEndListener(listener: () -> Unit) {
        addListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                listener()
            }
        })
    }

    //Extension functions
    private fun ObjectAnimator.addStartListener(listener: () -> Unit) {
        addListener(object : SimpleAnimatorListener() {
            override fun onAnimationStart(animation: Animator) {
                listener()
            }
        })
    }
}

