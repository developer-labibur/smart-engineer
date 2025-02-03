package com.rudhashi.smartengineer.ui.activities

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.ui.fragments.SmartHomeFragment
import com.rudhashi.smartengineer.utils.helpers.GetSet.sToast
import com.rudhashi.smartengineer.utils.helpers.Converters.dpToPx
import com.rudhashi.smartengineer.utils.helpers.GetSet.getCurrentDate
import com.rudhashi.smartengineer.databinding.ActivitySmartDashboardBinding
import com.rudhashi.smartengineer.ui.fragments.SmartBooksFragment
import com.rudhashi.smartengineer.ui.fragments.SmartSettingFragment
import com.rudhashi.smartengineer.ui.fragments.SmartUpdatesFragment
import com.rudhashi.smartengineer.managers.CUserManager
import com.rudhashi.smartengineer.ui.fragments.SmartStudyFragment
import com.rudhashi.smartengineer.utils.helpers.MakeFirebaseEasy
import com.rudhashi.smartengineer.utils.helpers.SmartConfig
import com.rudhashi.smartengineer.utils.themes.SmartThemeActivity

class SmartDashboardActivity : SmartThemeActivity() {

    private lateinit var binding: ActivitySmartDashboardBinding

    private val pictureListener: (String) -> Unit = { picture ->
        if (picture.isEmpty()) {
            binding.ivUserPic.setImageResource(R.drawable.student_avatar)
        } else {
            if (!this.isDestroyed && !this.isFinishing) {
                Glide.with(this)
                    .load(MakeFirebaseEasy.currentUserImage(picture))
                    .placeholder(R.drawable.student_avatar)
                    .transform(RoundedCorners(dpToPx(this, 10f)))
                    .into(binding.ivUserPic)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartDashboardBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Ensure the content fills the entire screen, including under the status bar
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setDailySmartPointsBonus()

        setupViewPager() // Setup the viewPager
        setupBottomNavigation() // Setup navigation
        setActiveTab(0) // Set the initial tab to Home (index 0)

        // Set Smart Engineering App Text Gradient -------------------------------------------
        setGradientTextColor()

        // Set Current User Data -------------------------------------------------------------
        CUserManager.apply {
            department {
                if (it.isEmpty()) binding.tvDepName.visibility =
                    View.GONE else binding.tvDepName.visibility = View.VISIBLE
            }
            name { if (it.isEmpty()) binding.tvProfile.hint = "Incomplete Profile" }
            name { binding.tvProfile.text = it }
            department { binding.tvDepName.text = it }
            picture(pictureListener)
        }
    }

    private fun setDailySmartPointsBonus() {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = FirebaseAuth.getInstance().currentUser?.let {
            firestore.collection("users").document(it.uid)
        }
        userRef?.get()?.addOnSuccessListener { document ->
            val currentDate = getCurrentDate() // Get today's date

            if (document.exists()) {
                // User already exists in Firestore
                val currentPoints = document.getLong("points")?.toInt() ?: 0
                val lastBonusDate = document.getString("lastBonusDate")
                val joiningDate = document.getString("joiningDate")
                val role = document.getString("role")

                // Check if joiningDate is missing and update it
                if (joiningDate == null) {
                    userRef.update("joiningDate", currentDate).addOnSuccessListener {
                        sToast(this, "Joining date added for existing user.")
                    }.addOnFailureListener {
                        sToast(this, "Failed to update joining date.")
                    }
                }

                // Check and award daily bonus
                if (lastBonusDate != currentDate) {
                    val newPoints = currentPoints + SmartConfig.DAILY_LOGIN_BONUS_POINT
                    val updates = mapOf(
                        "points" to newPoints,
                        "lastBonusDate" to currentDate
                    )
                    userRef.update(updates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            sToast(this, "Points added successfully.")
                        } else {
                            sToast(this, "Failed to add bonus points!")
                        }
                    }
                }

                // Check if joiningDate is missing and update it
                if (role == null) {
                    userRef.update("role", "User").addOnSuccessListener {
                        sToast(this, "Role added for existing user.")
                    }.addOnFailureListener {
                        sToast(this, "Failed to update role.")
                    }
                }

            } else {
                // New user: Initialize account
                val newUser = hashMapOf(
                    "points" to SmartConfig.DAILY_LOGIN_BONUS_POINT,
                    "lastBonusDate" to currentDate,
                    "joiningDate" to currentDate,
                    "role" to "Free User"
                )
                userRef.set(newUser).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        sToast(this, "Welcome to Smart Engineer - E-Learning App!")
                    } else {
                        sToast(this, "Failed to create account!")
                    }
                }
            }
        }?.addOnFailureListener {
            sToast(this, "Failed to fetch user data!")
        }
    }


    // =====================================================================================================================
    // ========================     Setup Bottom Navigation and Main Container (ViewPager)      ============================
    // =====================================================================================================================
    private fun setupViewPager() {
        // Set up ViewPager with fragments
        val fragments = listOf(
            SmartHomeFragment(),
            SmartStudyFragment(),
            SmartUpdatesFragment(),
            SmartSettingFragment()
        )

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
    }

    private fun setupBottomNavigation() {

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
                0 -> {
                    tvHome.visibility = View.VISIBLE
                    tvBook.visibility = View.GONE
                    tvUpdates.visibility = View.GONE
                    tvSetting.visibility = View.GONE
                    llHome.setBackgroundResource(R.drawable.round_back_home_100)
                    llBook.setBackgroundResource(R.color.transparent)
                    llUpdates.setBackgroundResource(R.color.transparent)
                    llSetting.setBackgroundResource(R.color.transparent)
                }

                1 -> {
                    tvHome.visibility = View.GONE
                    tvBook.visibility = View.VISIBLE
                    tvUpdates.visibility = View.GONE
                    tvSetting.visibility = View.GONE
                    llHome.setBackgroundResource(R.color.transparent)
                    llBook.setBackgroundResource(R.drawable.round_back_book_100)
                    llUpdates.setBackgroundResource(R.color.transparent)
                    llSetting.setBackgroundResource(R.color.transparent)
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
                }

                else -> {
                    tvHome.visibility = View.GONE
                    tvBook.visibility = View.GONE
                    tvUpdates.visibility = View.GONE
                    tvSetting.visibility = View.VISIBLE
                    llHome.setBackgroundResource(R.color.transparent)
                    llBook.setBackgroundResource(R.color.transparent)
                    llUpdates.setBackgroundResource(R.color.transparent)
                    llSetting.setBackgroundResource(R.drawable.round_back_setting_100)
                }
            }

            // Sync ViewPager position with selected tab
            if (viewPager.currentItem != position) {
                viewPager.currentItem = position
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

    // =====================================================================================================================
    // ===================================     Activity Other Lifecycle Method     =========================================
    // =====================================================================================================================

    override fun onResume() {
        super.onResume()
        val displayFragment = intent.getStringExtra("DISPLAY").toString()
        when (displayFragment) {
            "Home" -> setActiveTab(0)
            "Setting" -> setActiveTab(3)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the picture listener to avoid callbacks after the activity is destroyed
        CUserManager.removePictureListener(pictureListener)
        CUserManager.cleanup()
    }
}