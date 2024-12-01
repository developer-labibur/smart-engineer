package com.rudhashi.smartengineer.view

import android.content.SharedPreferences
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivitySmartDashboardBinding
import com.rudhashi.smartengineer.fragment.SmartBooksFragment
import com.rudhashi.smartengineer.fragment.SmartHomeFragment
import com.rudhashi.smartengineer.fragment.SmartSettingFragment
import com.rudhashi.smartengineer.fragment.SmartUpdatesFragment
import com.rudhashi.smartengineer.model.User
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData

class SmartDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmartDashboardBinding

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private var database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("Users/$userId")

    private lateinit var sharedPreferences: SharedPreferences
    private var isNightMode: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Completely transparent Status Bar and Setup Main Container ------------------------------
        setContainerMakeTransparent()

        // Set Smart Engineering App Text Gradient ------------------------------
        setGradientTextColor()

        checkAndAwardDailyBonus() // Check and award daily bonus points
    }

    // =====================================================================================================================
    // =========================     Transparent Status Bar and Main Container (ViewPager)      ============================
    // =====================================================================================================================
    private fun setContainerMakeTransparent(){
        // Initialize the shared preferences
        sharedPreferences = getSharedPreferences(SmartData.PREF_KEY, MODE_PRIVATE)

        // Get the current theme mode
        isNightMode = sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)
        if (isNightMode as Boolean) { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
        setStatusBarFontColor(!isNightMode!!)

        // Set up ViewPager with fragments
        val fragments = listOf(
            SmartHomeFragment(),
            SmartBooksFragment(),
            SmartUpdatesFragment(),
            SmartSettingFragment()
        )

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        // Set height based on the currently selected page
        binding.apply {
            // Bottom Navigation Buttons Click Listeners
            llHome.setOnClickListener { setActiveTab(0) }
            llBook.setOnClickListener { setActiveTab(1) }
            llUpdates.setOnClickListener { setActiveTab(2) }
            llSetting.setOnClickListener { setActiveTab(3) }

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    setActiveTab(position)
                }
            })
        }
    }
    private fun setActiveTab(position: Int) {
        binding.apply {

            when (position) {
                1 -> {
                    tvHome.visibility = View.GONE
                    tvBook.visibility = View.VISIBLE
                    llHome.setBackgroundResource(R.color.transparent)
                    llBook.setBackgroundResource(R.drawable.round_back_book_100)
                }
                2 -> {
                    tvUpdates.visibility = View.VISIBLE
                    tvSetting.visibility = View.GONE
                    llUpdates.setBackgroundResource(R.drawable.round_back_updates_100)
                    llSetting.setBackgroundResource(R.color.transparent)
                }
                3 -> {
                    tvUpdates.visibility = View.GONE
                    tvSetting.visibility = View.VISIBLE
                    llUpdates.setBackgroundResource(R.color.transparent)
                    llSetting.setBackgroundResource(R.drawable.round_back_setting_100)
                }
                else -> {
                    tvHome.visibility = View.VISIBLE
                    tvBook.visibility = View.GONE
                    llHome.setBackgroundResource(R.drawable.round_back_home_100)
                    llBook.setBackgroundResource(R.color.transparent)
                }
            }

            if (position == 0 || position == 1) {
                tvUpdates.visibility = View.GONE
                tvSetting.visibility = View.GONE

                llUpdates.setBackgroundResource(R.color.transparent)
                llSetting.setBackgroundResource(R.color.transparent)

                val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f)
                scaleAnimation.duration = 200
                scaleAnimation.fillAfter = true
                if (position == 0) llHome.animation = scaleAnimation
                else llBook.animation = scaleAnimation
            } else {
                tvHome.visibility = View.GONE
                tvBook.visibility = View.GONE

                llHome.setBackgroundResource(R.color.transparent)
                llBook.setBackgroundResource(R.color.transparent)

                // create animation
                val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f)
                scaleAnimation.duration = 200
                scaleAnimation.fillAfter = true
                if (position == 2) llUpdates.animation = scaleAnimation
                else llSetting.animation = scaleAnimation

            }

            // Sync ViewPager position with selected tab
            if (viewPager.currentItem != position) {
                viewPager.currentItem = position
            }
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

    // =====================================================================================================================
    // ============================================     Gradient Text color     ============================================
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

    override fun onResume() {
        super.onResume()
        displayPointsDynamically()
    }

    // =====================================================================================================================
    // ============================================     Smart Point System     ============================================
    // =====================================================================================================================
    private fun checkAndAwardDailyBonus() {
        userRef.get().addOnSuccessListener { snapshot ->
            val currentDate = SmartData.getCurrentDate() // Get today's date
            val lastBonusDate = snapshot.child("lastBonusDate").value?.toString()
            val currentPoints = snapshot.child("points").value?.toString()?.toInt() ?: 0

            if (lastBonusDate != currentDate) {
                // User hasn't received the daily bonus today
                val newPoints = currentPoints + SmartConfig.DAILY_LOGIN_BONUS_POINT
                val updates = mapOf(
                    "points" to newPoints,
                    "lastBonusDate" to currentDate
                )
                userRef.updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "${SmartConfig.DAILY_LOGIN_BONUS_POINT} points bonus added!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to add bonus points!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun displayPointsDynamically() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convert snapshot to User object
                val user = snapshot.getValue(User::class.java)

                if (user?.department?.isEmpty() == true) binding.tvDepName.visibility = View.GONE
                else binding.tvDepName.visibility = View.VISIBLE
                if (user?.name?.isEmpty() == true) binding.tvProfile.hint = "Incomplete Profile"

                // Update the TextView with the new points
                binding.tvProfile.text = user?.name
                binding.tvDepName.text = user?.department
                binding.tvTaka.text = user?.points.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SmartDashboardActivity, "Failed to load points", Toast.LENGTH_SHORT).show()
            }
        })
        /* Get user saved department name from Firebase Realtime database and convert it to department code
        database.reference.child("Users").child(auth.uid!!).child("name").get()
            .addOnSuccessListener {
                binding.tvProfile.text = it.value.toString()
            }*/
    }
}