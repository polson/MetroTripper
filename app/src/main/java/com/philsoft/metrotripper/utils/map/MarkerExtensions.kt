package com.philsoft.metrotripper.utils.map

import android.animation.Animator
import android.animation.ValueAnimator
import com.google.android.gms.maps.model.Marker
import com.philsoft.metrotripper.app.ui.slidingpanel.SimpleAnimatorListener

fun Marker.fadeIn(animDuration: Int): ValueAnimator {
    return ValueAnimator.ofFloat(0f, 1f).apply {
        duration = animDuration.toLong()
        addUpdateListener { animation -> alpha = animation.animatedValue as Float }
        start()
    }
}

fun Marker.fadeOutAndRemove(animDuration: Int): ValueAnimator {
    return ValueAnimator.ofFloat(1f, 0f).apply {
        duration = animDuration.toLong()
        addUpdateListener { animation -> alpha = animation.animatedValue as Float }
        addEndListener { this@fadeOutAndRemove.remove() }
        start()
    }
}

private fun ValueAnimator.addEndListener(listener: () -> Unit) {
    this.addListener(object : SimpleAnimatorListener() {
        override fun onAnimationEnd(animation: Animator) {
            listener()
        }
    })
}
