package com.rudhashi.smartengineer.old.util

import android.view.View
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.rudhashi.smartengineer.R

class ParallaxPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        val parallaxView = view.findViewById<ImageView>(R.id.imageView)
        val scaleFactor = 0.85f + (1 - Math.abs(position)) * 0.15f

        parallaxView.translationX = -position * view.width / 2  // Parallax effect
        view.scaleX = scaleFactor  // Scale effect
        view.scaleY = scaleFactor
    }
}