package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivitySplashScreenBinding
import com.rudhashi.smartengineer.util.SmartData

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var sharedPreferences: SharedPreferences
    private var isNightMode: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the shared preferences
        sharedPreferences = getSharedPreferences(SmartData.PREF_KEY, MODE_PRIVATE)

        // Get the current theme mode
        isNightMode = sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)
        if (isNightMode as Boolean) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        auth = FirebaseAuth.getInstance()

        val upFromBottom = AnimationUtils.loadAnimation(this, R.anim.animation1)

        binding.imgLogo.animation = upFromBottom
        binding.tvSloganSmartEngineer.animation = upFromBottom

        // Delay the splash screen for 3 seconds, then move to AllUserActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in and move to AllUserActivity if true, otherwise move to VerificationActivity
            if (auth.currentUser != null) {
                // Start the home page activity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // New User => Start the Main activity
                startActivity(Intent(this, MainActivity::class.java))
            }
            // Close this activity
            finish()
        }, SmartData.SPLASH_SCREEN) // 2 seconds delay

    }
}