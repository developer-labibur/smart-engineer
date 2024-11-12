package com.rudhashi.smartengineer.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class DynamicHeightViewPager2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val viewPager2: ViewPager2 = ViewPager2(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    init {
        addView(viewPager2)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        viewPager2.adapter = adapter
    }

    fun getViewPager2(): ViewPager2 = viewPager2

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var maxHeight = 0

        // Loop through children to find the maximum height and set it as the height of the ViewPager2
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED)
            val childHeight = child.measuredHeight
            if (childHeight > maxHeight) maxHeight = childHeight
        }

        val finalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, finalHeightMeasureSpec)
    }
}
