package com.philsoft.metrotripper.app.ui.slidingpanel

import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.VelocityTracker

/**
 * A MotionEvent tracker that determines whether or not a drag occurred.  Motion events should be
 * passed into `trackEvent` and a drag can be checked via `isDragging`.  When a drag occurs or stops
 * the `dragListener` is called
 */
class VerticalDragDetector(private val dragListener: DragListener, private val dragThreshold: Float = 1000f) {
    companion object {
        private val PER_SECOND = 1000
    }

    var isDragging = false
        private set

    private var lastMoveX = 0f
    private var lastMoveY = 0f
    private var velocityTracker: VelocityTracker? = null

    fun trackEvent(ev: MotionEvent) {
        when (ev.action) {
            ACTION_DOWN -> {
                updateVelocity(ev)
            }

            ACTION_MOVE -> {
                updateVelocity(ev)
                val distanceY = getDistanceY(ev)
                val distanceX = getDistanceX(ev)
                if (isDragging) {
                    notifyDrag(distanceY)
                } else if (didStartVerticalDrag(distanceX, distanceY)) {
                    isDragging = true
                    notifyDrag(distanceY)
                }
            }

            ACTION_UP, ACTION_CANCEL -> {
                if (isDragging) {
                    updateVelocity(ev)
                    notifyDragStopped()
                }
                reset()
            }
        }
    }

    private fun getVelocity(): Float {
        return velocityTracker?.run {
            computeCurrentVelocity(PER_SECOND)
            yVelocity
        } ?: 0f
    }

    private fun updateVelocity(ev: MotionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(ev)
    }

    private fun didStartVerticalDrag(distanceX: Float, distanceY: Float): Boolean {
        if (Math.abs(getVelocity()) > dragThreshold) {
            return Math.abs(distanceX) < Math.abs(distanceY)
        }
        return false
    }

    private fun getDistanceX(ev: MotionEvent): Float {
        val result = ev.x - lastMoveX
        lastMoveX = ev.x
        return result
    }


    private fun getDistanceY(ev: MotionEvent): Float {
        val result = ev.y - lastMoveY
        lastMoveY = ev.y
        return result
    }


    private fun notifyDrag(distanceY: Float) {
        dragListener.onDrag(distanceY)
    }

    private fun notifyDragStopped() {
        dragListener.onDragStopped(getVelocity())
    }

    private fun reset() {
        velocityTracker?.clear()
        velocityTracker?.recycle()
        velocityTracker = null
        isDragging = false
        lastMoveX = 0f
        lastMoveY = 0f
    }

    interface DragListener {
        fun onDrag(distanceY: Float)

        fun onDragStopped(velocity: Float)
    }
}

