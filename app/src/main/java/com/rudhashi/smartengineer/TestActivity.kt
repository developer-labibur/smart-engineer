package com.rudhashi.smartengineer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.rudhashi.smartengineer.adapter.DynamicHeightPagerAdapter
import com.rudhashi.smartengineer.databinding.ActivityTestBinding
import com.rudhashi.smartengineer.model.Notice

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private lateinit var adapter: DynamicHeightPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = listOf(
            Notice(category = "Page 1", notice = "This is the description for page 1."),
            Notice(category = "Page 2", notice = "This is a longer description for page 2. It should make the ViewPager dynamically increase in height."),
            Notice(category = "Page 3", notice = "Short description."),
            Notice(category = "Page 4", notice = "This is another page with a longer description, which should also adjust the height accordingly.")
        )

        adapter = DynamicHeightPagerAdapter(items)
        binding.viewPager.adapter = adapter

        // Set height based on the currently selected page
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.viewPager.post {
                    updatePagerHeight(position)
                }
            }
        })
    }

    private fun updatePagerHeight(position: Int) {
        val view = (binding.viewPager[0] as RecyclerView).layoutManager?.findViewByPosition(position)
        view?.let {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            val height = view.measuredHeight
            if (binding.viewPager.layoutParams.height != height) {
                binding.viewPager.layoutParams = binding.viewPager.layoutParams.apply {
                    this.height = height
                }
            }
        }
    }
}