package com.softylasa.smartengineer.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.ActivitySmartPointBinding
import com.softylasa.smartengineer.managers.CUserManager
import com.softylasa.smartengineer.managers.DialogManager
import com.softylasa.smartengineer.managers.PointManager
import com.softylasa.smartengineer.utils.helpers.SmartConfig
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity

class SmartPointActivity : SmartThemeActivity() {

    private lateinit var binding: ActivitySmartPointBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartPointBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.apply {
            CUserManager.points { tvPoints.text =  "$it" }
            btnAds.text = getString(R.string.get_points, SmartConfig.WATCH_VIDEO_BONUS_POINT.toString())
            tvDailyBonusText.text = getString(R.string.daily_login_bonus, SmartConfig.DAILY_LOGIN_BONUS_POINT.toString())
            tvSignUpBonusText.text = getString(R.string.new_user_signup_bonus, SmartConfig.NEW_USER_SIGNUP_BONUS_POINT.toString())

            btnBack.setOnClickListener { finish() }
            btnProfile.setOnClickListener { startActivity(Intent(this@SmartPointActivity, ProfileActivity::class.java)) }
            btnAds.setOnClickListener{
                DialogManager.showWatchAdDialog(this@SmartPointActivity, txtHeading = R.string.watch_ad, txtInfo = R.string.watch_a_video_ad_and_earn)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Sync any pending rewards in case of offline updates
        PointManager.syncPendingRewards(this)
    }
}