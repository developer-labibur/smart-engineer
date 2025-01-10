package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.smartengineer.MyAdmobApp
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.dashboard.SmartDashboardActivity
import com.rudhashi.smartengineer.databinding.ActivitySplashScreenBinding
import com.rudhashi.smartengineer.manager.AdManager
import com.rudhashi.smartengineer.manager.PointManager
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartThemeActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var auth: FirebaseAuth
    val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        // Sync pending rewards when the app starts
        PointManager.syncPendingRewards(this)

        auth = FirebaseAuth.getInstance()

        // Animate the logo and slogan
        val upFromBottom = AnimationUtils.loadAnimation(this, R.anim.animation1)
        binding.imgLogo.animation = upFromBottom
        binding.tvSloganSmartEngineer.animation = upFromBottom

        // Preload AdVisibility from Firebase
        setAdVisibility()

        // Preload rewarded ad if ads are visible
        if (SmartConfig.isAdsVisible(this)) {
            AdManager.loadRewardedAd(this)
            (application as? MyAdmobApp)?.showAdIfAvailable(
                this@SplashScreenActivity,
                object : MyAdmobApp.OnShowAdCompleteListener {
                    override fun onShowAdComplete() { navigateWithDelay() }
                    override fun onShowAdDismiss() { navigateActivity() }
                }
            )
        } else navigateWithDelay()
    } // End OnCreate
    private fun navigateWithDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateActivity()
        }, SmartData.SPLASH_SCREEN) // 2 seconds delay
    }
    private fun navigateActivity() {
        if (auth.currentUser != null) {
            // User is logged in, navigate to the Dashboard
            startActivity(Intent(this@SplashScreenActivity, SmartDashboardActivity::class.java))
        } else {
            // New User => Navigate to MainActivity
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
        }
        finish() // Close the splash screen
    }
    private fun setAdVisibility() {
        val databaseRef = database.getReference("AppConfig/AdmobAds/AdVisibility")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val showAds = snapshot.getValue(Boolean::class.java) ?: false
                val sharedPreferences = getSharedPreferences(SmartData.ADMOB_KEY, MODE_PRIVATE)
                sharedPreferences.edit().putBoolean(SmartData.VISIBILITY_KEY, showAds).apply()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching AdVisibility: ${error.message}")
                // Set a default value in case of failure
                val sharedPreferences = getSharedPreferences(SmartData.ADMOB_KEY, MODE_PRIVATE)
                sharedPreferences.edit().putBoolean(SmartData.VISIBILITY_KEY, false).apply()
            }
        })
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
                    View.SYSTEM_UI_FLAG_IMMERSIVE
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