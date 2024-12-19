package com.rudhashi.smartengineer.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivitySmartPointBinding
import com.rudhashi.smartengineer.model.User
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartThemeActivity

class SmartPointActivity : SmartThemeActivity() {

    private lateinit var binding: ActivitySmartPointBinding

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private var database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("Users/$userId")

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
            btnAds.text = getString(R.string.get_points, SmartConfig.WATCH_VIDEO_BONUS_POINT.toString())
            tvDailyBonusText.text = getString(R.string.daily_login_bonus, SmartConfig.DAILY_LOGIN_BONUS_POINT.toString())
            tvSignUpBonusText.text = getString(R.string.new_user_signup_bonus, SmartConfig.NEW_USER_SIGNUP_BONUS_POINT.toString())

            btnBack.setOnClickListener { finish() }
            btnProfile.setOnClickListener { startActivity(Intent(this@SmartPointActivity, ProfileActivity::class.java)) }
        }
    }

    private fun displayPointsDynamically() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convert snapshot to User object
                val user = snapshot.getValue(User::class.java)
                binding.tvPoints.text = user?.points.toString()
            }

            override fun onCancelled(error: DatabaseError) { SmartData.lToast(this@SmartPointActivity, "Failed to load points") }
        })
    }

    override fun onResume() {
        super.onResume()
        displayPointsDynamically()
    }
}