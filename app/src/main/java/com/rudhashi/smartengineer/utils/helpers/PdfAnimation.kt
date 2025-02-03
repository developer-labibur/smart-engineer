package com.rudhashi.smartengineer.utils.helpers


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.ymg.pdf.viewer.PDFView

object PdfAnimation {

    // Shrink and Expand Animation
    fun applyShrinkExpandAnimation(view: PDFView) {
        val shrinkX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f)
        val shrinkY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f)
        val expandX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f)
        val expandY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            AnimatorSet().apply {
                playTogether(shrinkX, shrinkY)
                duration = 150
            },
            AnimatorSet().apply {
                playTogether(expandX, expandY)
                duration = 150
            }
        )
        animatorSet.start()
    }

    // Slide Animation
    fun applySlideAnimation(view: PDFView, isForward: Boolean) {
        val startTranslation = if (isForward) view.width.toFloat() else -view.width.toFloat()
        val slideAnimator = ObjectAnimator.ofFloat(view, "translationX", startTranslation, 0f)
        slideAnimator.duration = 500
        slideAnimator.interpolator = DecelerateInterpolator()

        slideAnimator.start()
    }


    // Flip Over Animation
    fun applyFlipOverAnimation(view: PDFView, isHorizontal: Boolean) {

        val property = if (isHorizontal) "rotationY" else "rotationX" // Horizontal flip or Vertical flip
        val flipAnimator = ObjectAnimator.ofFloat(view, property, 0f, 180f)
        flipAnimator.duration = 500 // Adjust duration for the flip speed
        flipAnimator.interpolator = AccelerateDecelerateInterpolator()

        flipAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // Reset the rotation after the flip animation ends
                if (isHorizontal) {
                    view.rotationY = 0f
                } else {
                    view.rotationX = 0f
                }
            }
        })

        flipAnimator.start()
    }

    // Cube Rotation Animation
    fun applyCubeRotationAnimation(view: PDFView, isHorizontal: Boolean) {
        val property = if (isHorizontal) "rotationY" else "rotationX"
        val cubeAnimator = ObjectAnimator.ofFloat(view, property, 0f, 90f, 0f)
        cubeAnimator.duration = 600
        cubeAnimator.interpolator = DecelerateInterpolator()

        cubeAnimator.start()
    }

    // Carousel Animation
    fun applyCarouselAnimation(view: PDFView) {
        val rotationAnimator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 45f, -45f, 0f)
        rotationAnimator.duration = 800
        rotationAnimator.interpolator = AccelerateDecelerateInterpolator()

        rotationAnimator.start()
    }

}