package com.rudhashi.smartengineer

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rudhashi.smartengineer.databinding.ActivityTestBinding
import com.rudhashi.smartengineer.util.SmartThemeActivity
import com.rudhashi.smartengineer.util.ThemeHelper
import com.rudhashi.smartengineer.dashboard.SmartDashboardActivity

class TestActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Ensure the content fills the entire screen, including under the status bar
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var isDarkMode = ThemeHelper.isDarkMode(this) // Load the current theme state
        updateTheme(isDarkMode)
        ThemeHelper.applyTheme(isDarkMode) // Apply the saved theme

        // Set up the switch listener
        binding.sideBarBtnSettings.setOnClickListener {
            if (isDarkMode){
                ThemeHelper.saveThemePreference(this, true)
                ThemeHelper.applyTheme(true) // Apply the theme
                isDarkMode = !isDarkMode
                updateTheme(true)
                val intent = Intent(this, SmartDashboardActivity::class.java)
                intent.putExtra("DISPLAY", "Home")
                startActivity(intent)
            } else {
                ThemeHelper.saveThemePreference(this, false)
                ThemeHelper.applyTheme(true) // Apply the theme
                isDarkMode = !isDarkMode
                updateTheme(false)
                val intent = Intent(this, SmartDashboardActivity::class.java)
                intent.putExtra("DISPLAY", "Home")
                startActivity(intent)
            }

        }
    }
    private fun updateTheme(isNightMode: Boolean) {
        if (isNightMode) {
            binding.sideBarBtnSettings.setBackgroundResource(R.drawable.night_mode_thumb_true)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            binding.sideBarBtnSettings.setBackgroundResource(R.drawable.night_mode_thumb_false)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}