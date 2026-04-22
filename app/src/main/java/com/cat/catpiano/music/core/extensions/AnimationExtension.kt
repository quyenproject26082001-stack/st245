package com.cat.catpiano.music.core.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

// Hiệu ứng nhấn (scale nhỏ rồi về lại)
fun View.animateScaleEffect(scaleDownFactor: Float = 0.9f, duration: Long = 100L) {
    val scaleDownX = ObjectAnimator.ofFloat(this, "scaleX", 1f, scaleDownFactor)
    val scaleDownY = ObjectAnimator.ofFloat(this, "scaleY", 1f, scaleDownFactor)
    scaleDownX.duration = duration
    scaleDownY.duration = duration

    val scaleUpX = ObjectAnimator.ofFloat(this, "scaleX", scaleDownFactor, 1f)
    val scaleUpY = ObjectAnimator.ofFloat(this, "scaleY", scaleDownFactor, 1f)
    scaleUpX.duration = duration
    scaleUpY.duration = duration

    AnimatorSet().apply {
        play(scaleDownX).with(scaleDownY)
        play(scaleUpX).with(scaleUpY).after(scaleDownX)
        start()
    }
}

