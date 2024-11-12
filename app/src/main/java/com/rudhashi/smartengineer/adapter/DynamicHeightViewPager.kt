package com.rudhashi.smartengineer.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager

class DynamicHeightViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpecAdjusted = heightMeasureSpec

        val currentView = getChildAt(currentItem)
        if (currentView != null) {
            currentView.measure(widthMeasureSpec, View.MeasureSpec.UNSPECIFIED)
            val height = currentView.measuredHeight
            heightMeasureSpecAdjusted = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpecAdjusted)
    }
}
