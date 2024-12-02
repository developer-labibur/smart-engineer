package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivitySmartDashboardBinding
import com.rudhashi.smartengineer.databinding.DialogDailyRewardBinding
import com.rudhashi.smartengineer.databinding.DialogLogoutWarningBinding
import com.rudhashi.smartengineer.fragment.SmartBooksFragment
import com.rudhashi.smartengineer.fragment.SmartHomeFragment
import com.rudhashi.smartengineer.fragment.SmartSettingFragment
import com.rudhashi.smartengineer.fragment.SmartUpdatesFragment
import com.rudhashi.smartengineer.model.User
import com.rudhashi.smartengineer.util.GoogleAuthClient
import com.rudhashi.smartengineer.util.NightModeThemes
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import kotlinx.coroutines.launch

class SmartDashboardActivity : NightModeThemes() {

    private lateinit var binding: ActivitySmartDashboardBinding

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private var database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("Users/$userId")

    //private var isNightMode: Boolean? = null

    private var displayFragment = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up ViewPager with fragments
        val fragments = listOf(SmartHomeFragment(), SmartBooksFragment(), SmartUpdatesFragment(), SmartSettingFragment())

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        //Log.i("TEST_SL", "Display Fragment: Else")
        setActiveTab(0)
        displayFragment = intent.getStringExtra("DISPLAY").toString()

//        else if (displayFragment == "Setting") {
//            Log.i("TEST_SL", "Display Fragment: Try Block => $displayFragment")
//            displayFragment = ""
//            setActiveTab(3)
//            displayFragment = ""
//        }

        setContainerMakeTransparent()

        // Set Smart Engineering App Text Gradient ------------------------------
        setGradientTextColor()

        checkAndAwardDailyBonus() // Check and award daily bonus points
    }

    // =====================================================================================================================
    // =========================     Transparent Status Bar and Main Container (ViewPager)      ============================
    // =====================================================================================================================
    private fun setContainerMakeTransparent(){

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
                    displayFragment = ""
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

                    displayFragment = ""
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
    // ============================================     Smart Point System     ============================================
    // =====================================================================================================================
    @SuppressLint("InflateParams")
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

                        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
                        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_daily_reward, null)
                        val dialogBinding = DialogDailyRewardBinding.bind(dialogView)
                        builder.setView(dialogBinding.root)
                        builder.setCanceledOnTouchOutside(false)
                        builder.show()

                        dialogBinding.btnAllPoints.setOnClickListener {
                            startActivity(Intent(this, ProfileActivity::class.java))
                            builder.dismiss()
                        }
                        dialogBinding.btnClose.setOnClickListener { builder.dismiss() }

                        if (builder.window != null) builder.window!!.setBackgroundDrawable(
                            ColorDrawable(0)
                        )
                        builder.show()
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

    override fun onResume() {
        super.onResume()
        displayPointsDynamically()

        if (displayFragment == "Home") setActiveTab(0)
        if (displayFragment == "Setting") setActiveTab(3)
    }
}