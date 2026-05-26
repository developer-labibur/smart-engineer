package com.softylasa.smartengineer.ui.activities


import android.content.pm.PackageManager
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.ui.fragments.SmartHomeFragment
import com.softylasa.smartengineer.utils.helpers.Converters.dpToPx
import com.softylasa.smartengineer.utils.helpers.GetSet.getCurrentDate
import com.softylasa.smartengineer.databinding.ActivitySmartDashboardBinding
import com.softylasa.smartengineer.ui.fragments.SmartSettingFragment
import com.softylasa.smartengineer.managers.CUserManager
import com.softylasa.smartengineer.ui.fragments.SmartStudyFragment
import com.softylasa.smartengineer.utils.helpers.EasySdk.setToast
import com.softylasa.smartengineer.utils.helpers.MakeFirebaseEasy
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.softylasa.smartengineer.R.layout.layout_disclaimer_dialog
import com.softylasa.smartengineer.databinding.LayoutDisclaimerDialogBinding
import com.softylasa.smartengineer.ui.fragments.SmartBooksFragment

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

        // Check if the device is running Android 13 (API level 33) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the app already has permission to post notifications
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // Check if battery optimizations are enabled for the app
        if (isBatteryOptimizationEnabled()) {
            // Prompt the user to open the battery optimization settings
            requestBatteryOptimizationPermission()
        }

        setJoiningDate()

        setupViewPager() // Setup the viewPager
        setupBottomNavigation() // Setup navigation
        setActiveTab(0) // Set the initial tab to Home (index 0)

        // Set Smart Engineering App Text Gradient -------------------------------------------
        setGradientTextColor()

        // Set Current User Data -------------------------------------------------------------
        CUserManager.initialize()
        CUserManager.apply {
            department {
                if (it.isEmpty()) binding.tvDepName.visibility =
                    View.GONE else binding.tvDepName.visibility = View.VISIBLE
            }
            name { if (it.isEmpty()) binding.tvProfile.hint = "Incomplete Profile" }
            name { binding.tvProfile.text = it }
            role { binding.tvRole.text = it }
            department { binding.tvDepName.text = it }
            picture(pictureListener)
        }

        // Disclaimer Dialogs
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstLogin = sharedPrefs.getBoolean("is_first_login", true)

        if (isFirstLogin) {
            showDisclaimerDialog()
            sharedPrefs.edit().putBoolean("is_first_login", false).apply()
        }
    }

    // Disclaimer Dialogs -------------------------------------------------------------------
    @SuppressLint("InflateParams")
    private fun showDisclaimerDialog() {
        val builder = AlertDialog.Builder(this).create()
        val dialogView = LayoutInflater.from(this).inflate(layout_disclaimer_dialog, null)
        val dialogBinding = LayoutDisclaimerDialogBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        // Handle Button Click
        dialogBinding.buttonAction.setOnClickListener { builder.dismiss() }

        // Show Dialog
        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    // Method to check if battery optimization is enabled
    private fun isBatteryOptimizationEnabled(): Boolean {
        val pm = applicationContext.getSystemService(POWER_SERVICE) as android.os.PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName).not()
    }

    // Method to request battery optimization permission
    @SuppressLint("BatteryLife")
    private fun requestBatteryOptimizationPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${packageName}")
        startActivity(intent)
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now show notifications
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setJoiningDate() {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = FirebaseAuth.getInstance().currentUser?.let {
            firestore.collection("users").document(it.uid)
        }
        userRef?.get()?.addOnSuccessListener { document ->
            val currentDate = getCurrentDate() // Get today's date

            if (document.exists()) {
                // User already exists in Firestore
                val joiningDate = document.getString("joiningDate")
                val role = document.getString("role")

                // Check if joiningDate is missing and update it
                if (joiningDate == null) {
                    userRef.update("joiningDate", currentDate).addOnSuccessListener {
                        setToast("Joining date added for existing user.")
                    }.addOnFailureListener {
                        setToast("Failed to update joining date.")
                    }
                }

                // Check if joiningDate is missing and update it
                if (role == null) {
                    userRef.update("role", "Free User").addOnSuccessListener {
                        setToast("Role added for existing user.")
                    }.addOnFailureListener {
                        setToast("Failed to update role.")
                    }
                }

            } else {
                // New user: Initialize account
                val newUser = hashMapOf(
                    "joiningDate" to currentDate,
                    "role" to "Free User"
                )
                userRef.set(newUser).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        setToast("Welcome to TechLib - E-Learning App!")
                    } else {
                        setToast("Failed to create account!")
                    }
                }
            }
        }?.addOnFailureListener {
            setToast("Failed to fetch user data!")
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
            SmartBooksFragment(),
            SmartSettingFragment()
        )

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = true // Enable swipe gestures if needed
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
        binding.viewPager.setCurrentItem(position, true) // Smooth scroll to selected page

        // Define all TextViews and Layouts in Lists for cleaner updates
        val textViews = listOf(binding.tvHome, binding.tvBook, binding.tvUpdates, binding.tvSetting)
        val layouts = listOf(binding.llHome, binding.llBook, binding.llUpdates, binding.llSetting)
        val backgroundResources = listOf(
            R.drawable.round_back_home_100,
            R.drawable.round_back_book_100,
            R.drawable.round_back_updates_100,
            R.drawable.round_back_setting_100
        )

        // Hide all text views and reset backgrounds
        textViews.forEach { it.visibility = View.GONE }
        layouts.forEach { it.setBackgroundResource(R.color.transparent) }

        // Show selected text and set background
        textViews[position].visibility = View.VISIBLE
        layouts[position].setBackgroundResource(backgroundResources[position])

        // Sync ViewPager if needed
        if (binding.viewPager.currentItem != position) {
            binding.viewPager.currentItem = position
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