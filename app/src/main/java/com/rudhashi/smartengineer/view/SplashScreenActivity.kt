package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.smartengineer.MyAdmobApp
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivitySplashScreenBinding
import com.rudhashi.smartengineer.util.NightModeThemes
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartThemeActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : SmartThemeActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var auth: FirebaseAuth
    val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val upFromBottom = AnimationUtils.loadAnimation(this, R.anim.animation1)

        binding.imgLogo.animation = upFromBottom
        binding.tvSloganSmartEngineer.animation = upFromBottom

        // Preload AdVisibility from Firebase
        setAdVisibility()

        if (SmartConfig.isAdsVisible(this)){
            (application as? MyAdmobApp)?.showAdIfAvailable(this@SplashScreenActivity, object : MyAdmobApp.OnShowAdCompleteListener{
                override fun onShowAdComplete() {
                    Handler(Looper.getMainLooper()).postDelayed({
                        navigateActivity()
                    }, SmartData.SPLASH_SCREEN) // 2 seconds delay
                }

                override fun onShowAdDismiss() {
                    navigateActivity()
                }
            })
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                navigateActivity()
            }, SmartData.SPLASH_SCREEN) // 2 seconds delay
        }

    } // End OnCreate

    private fun navigateActivity() {
        // Check if user is already logged in and move to AllUserActivity if true, otherwise move to VerificationActivity
        if (auth.currentUser != null) {
            // Start the Dashboard activity
            startActivity(Intent(this@SplashScreenActivity, SmartDashboardActivity::class.java))
        } else {
            // New User => Start the Main activity
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
        }
        // Close this activity
        finish()
    }
    private fun setAdVisibility() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("AppConfig/AdmobAds/AdVisibility")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("CommitPrefEdits")
            override fun onDataChange(snapshot: DataSnapshot) {
                val showAds = snapshot.getValue(Boolean::class.java) ?: false
                val sharedPreferences = getSharedPreferences(SmartData.ADMOB_KEY, MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean(SmartData.VISIBILITY_KEY, showAds)
                editor.apply()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching AdVisibility: ${error.message}")
            }
        })
    }
}