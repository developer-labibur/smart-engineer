package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivitySplashScreenBinding
import com.rudhashi.smartengineer.util.NightModeThemes
import com.rudhashi.smartengineer.util.SmartData

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : NightModeThemes() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)





        // Defer status bar changes
        binding.root.post {
            setStatusBarFontColor(true) // Adjust based on your logic
        }





        auth = FirebaseAuth.getInstance()

        val upFromBottom = AnimationUtils.loadAnimation(this, R.anim.animation1)

        binding.imgLogo.animation = upFromBottom
        binding.tvSloganSmartEngineer.animation = upFromBottom

        // Delay the splash screen for 3 seconds, then move to AllUserActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in and move to AllUserActivity if true, otherwise move to VerificationActivity
            if (auth.currentUser != null) {
                // Start the Dashboard activity
                startActivity(Intent(this, SmartDashboardActivity::class.java))
            } else {
                // New User => Start the Main activity
                startActivity(Intent(this, MainActivity::class.java))
            }
            // Close this activity
            finish()
        }, SmartData.SPLASH_SCREEN) // 2 seconds delay

    }
}