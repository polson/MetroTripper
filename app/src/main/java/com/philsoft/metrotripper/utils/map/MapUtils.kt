package com.philsoft.metrotripper.utils.map

import android.animation.Animator
import android.animation.ValueAnimator
import com.google.android.gms.maps.model.Marker
import timber.log.Timber

object MapUtils {

    fun fadeOutMarkerAndRemove(marker: Marker, duration: Int): ValueAnimator {
        Timber.d("Fading marker: " + marker.title)
        val ani = ValueAnimator.ofFloat(1f, 0f)
        ani.duration = duration.toLong()
        ani.addUpdateListener { animation -> marker.alpha = animation.animatedValue as Float }
        ani.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                marker.remove()
            }

            override fun onAnimationCancel(animation: Animator) {
                marker.remove()
            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        ani.start()
        return ani
    }


    fun fadeInMarker(marker: Marker, duration: Int): ValueAnimator {
        val ani = ValueAnimator.ofFloat(0f, 1f)
        ani.duration = duration.toLong()
        ani.addUpdateListener { animation -> marker.alpha = animation.animatedValue as Float }
        ani.start()
        return ani
    }
}
