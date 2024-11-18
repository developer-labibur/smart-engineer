package com.rudhashi.smartengineer

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.rudhashi.smartengineer.adapter.FragmentPageAdapter
import com.rudhashi.smartengineer.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private lateinit var adapter: FragmentPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setGradientTextColor()

        adapter = FragmentPageAdapter(supportFragmentManager,lifecycle)

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

    // =====================================================================================================================
    // =====================     Gradient Text color  &  ViewPager Dynamic Height                  =========================
    // =====================================================================================================================
    private fun setGradientTextColor(){
        binding.apply {
            // Gradient Text color Set -----------------------------------------------------------------------------------------
            tvSmart.paint.shader = LinearGradient(0f, 0f, tvSmart.paint.measureText(tvSmart.text.toString()), tvSmart.textSize, intArrayOf(
                resources.getColor(R.color.gradient_start, null),
                resources.getColor(R.color.gradient_start, null),
                resources.getColor(R.color.gradient_end, null)
            ), null, Shader.TileMode.CLAMP)
            tvE.paint.shader = LinearGradient(0f, 0f, tvE.paint.measureText(tvE.text.toString()), tvE.textSize, intArrayOf(
                resources.getColor(R.color.clr2_1, null),
                resources.getColor(R.color.clr1_1, null),
                resources.getColor(R.color.clr2_1, null)
            ), null, Shader.TileMode.CLAMP)
            tvEngineer.paint.shader = LinearGradient(0f, 0f, tvEngineer.paint.measureText(tvEngineer.text.toString()), tvEngineer.textSize, intArrayOf(
                resources.getColor(R.color.clr2_1_1, null),
                resources.getColor(R.color.clr2_1_2, null),
                resources.getColor(R.color.clr2_1_3, null)
            ), null, Shader.TileMode.CLAMP)
        }
    }
}