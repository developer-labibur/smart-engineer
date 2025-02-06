package com.softylasa.smartengineer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.softylasa.smartengineer.data.constants.Constants.IS_FIRST_TIME_KEY
import com.softylasa.smartengineer.data.constants.Constants.ONBOARDING_KEY
import com.softylasa.smartengineer.databinding.ActivityOnboardingBinding
import com.softylasa.smartengineer.ui.fragments.OnboardingFragment
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity

class OnboardingActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide the status bar and navigation bar
        hideSystemUI()

        // Initialize the ViewPager adapter with the 3 onboarding pages
        onboardingAdapter = OnboardingAdapter(this)
        binding.viewPager.adapter = onboardingAdapter

        // Set up DotsIndicator for ViewPager2
        binding.dotsIndicator.attachTo(binding.viewPager)

        // Handle "Next" button functionality
        binding.btnNext.setOnClickListener {
            binding.viewPager.currentItem += 1
        }
        binding.cvGetStarted.setOnClickListener {
            if (binding.viewPager.currentItem == 3) {
                // If on the last page, go to AuthActivity
                navigateToAuthActivity()
            }
        }

        // Handle "Skip" button functionality
//        binding.btnSkip.setOnClickListener {
//            navigateToAuthActivity()
//        }

        // Set up ViewPager2 page change callback
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButtons(position)
            }
        })
    }

    private fun updateButtons(position: Int) {
        if (position == 3) {
            //binding.btnNext.text = "Get Started"
            binding.cvGetStarted.visibility = View.VISIBLE
            binding.btnNext.visibility = View.INVISIBLE
        } else {
            //binding.btnNext.text = "Next"
            binding.btnNext.visibility = View.VISIBLE
            binding.cvGetStarted.visibility = View.INVISIBLE
        }
    }

    private fun navigateToAuthActivity() {
        val sharedPreferences = getSharedPreferences(ONBOARDING_KEY, MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(IS_FIRST_TIME_KEY, false).apply()

        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }


    inner class OnboardingAdapter(activity: OnboardingActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return 4 // There are 4 onboarding screens
        }

        override fun createFragment(position: Int): Fragment {
            return OnboardingFragment.newInstance(position)
        }
    }

    private fun hideSystemUI() {
        // For Android 11 (API 30) and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // For Android 10 (API 29) and below
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
    }

    // Optional: Restore the UI visibility when the Activity pauses
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }
}