package com.rudhashi.smartengineer.view

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.FragmentPageAdapter
import com.rudhashi.smartengineer.databinding.ActivitySmartDashboardBinding
import com.rudhashi.smartengineer.fragment.ExploreFragment
import com.rudhashi.smartengineer.fragment.HomeFragment
import com.rudhashi.smartengineer.fragment.LoginFragment
import com.rudhashi.smartengineer.fragment.NoticeFragment
import com.rudhashi.smartengineer.util.SmartData

class SmartDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmartDashboardBinding
    private lateinit var adapter: FragmentPageAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var isNightMode: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the shared preferences
        sharedPreferences = getSharedPreferences(SmartData.PREF_KEY, MODE_PRIVATE)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Get the current theme mode
        isNightMode = sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)
        if (isNightMode as Boolean) { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
        setStatusBarFontColor(!isNightMode!!)

        setGradientTextColor()

        adapter = FragmentPageAdapter(supportFragmentManager, lifecycle)

        // Set height based on the currently selected page
        binding.apply {
            viewPager.adapter = adapter

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    //viewPager.post { updatePagerHeight(position) }
                    super.onPageSelected(position)
                    when (position) {
                        1 -> {
                            tvHome.visibility = View.GONE
                            tvBook.visibility = View.VISIBLE
                            tvUpdates.visibility = View.GONE
                            tvSetting.visibility = View.GONE
                            llHome.setBackgroundResource(R.color.transparent)
                            llBook.setBackgroundResource(R.drawable.round_back_book_100)
                            llUpdates.setBackgroundResource(R.color.transparent)
                            llSetting.setBackgroundResource(R.color.transparent)

                            // create animation
                            val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f)
                            scaleAnimation.duration = 200
                            scaleAnimation.fillAfter = true
                            llBook.animation = scaleAnimation
                        }
                        2 -> {
                            tvHome.visibility = View.GONE
                            tvBook.visibility = View.GONE
                            tvUpdates.visibility = View.VISIBLE
                            tvSetting.visibility = View.GONE
                            llHome.setBackgroundResource(R.color.transparent)
                            llBook.setBackgroundResource(R.color.transparent)
                            llUpdates.setBackgroundResource(R.drawable.round_back_updates_100)
                            llSetting.setBackgroundResource(R.color.transparent)

                            // create animation
                            val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f)
                            scaleAnimation.duration = 200
                            scaleAnimation.fillAfter = true
                            llUpdates.animation = scaleAnimation
                        }
                        3 -> {
                            tvHome.visibility = View.GONE
                            tvBook.visibility = View.GONE
                            tvUpdates.visibility = View.GONE
                            tvSetting.visibility = View.VISIBLE
                            llHome.setBackgroundResource(R.color.transparent)
                            llBook.setBackgroundResource(R.color.transparent)
                            llUpdates.setBackgroundResource(R.color.transparent)
                            llSetting.setBackgroundResource(R.drawable.round_back_setting_100)

                            // create animation
                            val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f)
                            scaleAnimation.duration = 200
                            scaleAnimation.fillAfter = true
                            llSetting.animation = scaleAnimation
                        }
                        else -> {
                            tvHome.visibility = View.VISIBLE
                            tvBook.visibility = View.GONE
                            tvUpdates.visibility = View.GONE
                            tvSetting.visibility = View.GONE
                            llHome.setBackgroundResource(R.drawable.round_back_home_100)
                            llBook.setBackgroundResource(R.color.transparent)
                            llUpdates.setBackgroundResource(R.color.transparent)
                            llSetting.setBackgroundResource(R.color.transparent)

                            // create animation
                            val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f)
                            scaleAnimation.duration = 200
                            scaleAnimation.fillAfter = true
                            llHome.animation = scaleAnimation
                        }
                    }
                }
            })

            // Set Bottom Navigation buttons setOnClickListener
            llHome.setOnClickListener { viewPager.currentItem = 0 }
            llBook.setOnClickListener { viewPager.currentItem = 1 }
            llUpdates.setOnClickListener { viewPager.currentItem = 2 }
            llSetting.setOnClickListener { viewPager.currentItem = 3 }
        }
    }

    private fun updatePagerHeight(position: Int) {
        val view =
            (binding.viewPager[0] as RecyclerView).layoutManager?.findViewByPosition(position)
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
    private fun setGradientTextColor() {
        binding.apply {
            // Gradient Text color Set -----------------------------------------------------------------------------------------
            tvSmart.paint.shader = LinearGradient(
                0f,
                0f,
                tvSmart.paint.measureText(tvSmart.text.toString()),
                tvSmart.textSize,
                intArrayOf(
                    resources.getColor(R.color.gradient_start, null),
                    resources.getColor(R.color.gradient_start, null),
                    resources.getColor(R.color.gradient_end, null)
                ),
                null,
                Shader.TileMode.CLAMP
            )
            tvE.paint.shader = LinearGradient(
                0f, 0f, tvE.paint.measureText(tvE.text.toString()), tvE.textSize, intArrayOf(
                    resources.getColor(R.color.clr2_1, null),
                    resources.getColor(R.color.clr1_1, null),
                    resources.getColor(R.color.clr2_1, null)
                ), null, Shader.TileMode.CLAMP
            )
            tvEngineer.paint.shader = LinearGradient(
                0f,
                0f,
                tvEngineer.paint.measureText(tvEngineer.text.toString()),
                tvEngineer.textSize,
                intArrayOf(
                    resources.getColor(R.color.clr2_1_1, null),
                    resources.getColor(R.color.clr2_1_2, null),
                    resources.getColor(R.color.clr2_1_3, null)
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
    }

    private fun setStatusBarFontColor(isDarkText: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isDarkText) {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                window.insetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (isDarkText) {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
    }
}