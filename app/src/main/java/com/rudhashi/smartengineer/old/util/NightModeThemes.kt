package com.rudhashi.smartengineer.old.util

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

open class NightModeThemes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make status bar transparent
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val sharedPreferences = getSharedPreferences(SmartData.PREF_KEY, MODE_PRIVATE)

        // Set the initial state of the switch based on saved preference
        val isDarkModeOn = sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)

        // Apply the saved theme
        applyTheme(isDarkModeOn)

        // Ensure UI updates after the theme change
        window.decorView.post {
            setStatusBarFontColor(!isDarkModeOn) // Set status bar text color based on theme
        }
    }

    private fun applyTheme(isDarkModeOn: Boolean) {
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    protected fun setStatusBarFontColor(isDarkText: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 10 (API 29) and above, use InsetsController for status bar appearance
            val controller = window.insetsController
            controller?.let {
                if (isDarkText) {
                    // Light status bar text (dark theme)
                    it.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    // Dark status bar text (light theme)
                    it.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android 6 to 9 (API 23 - 28), use systemUiVisibility for status bar text color
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (isDarkText) {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Light text color
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Dark text color
            }
        } else {
            // For Android versions below API 23, this approach will not work
            Log.w("NightModeThemes", "Status bar color cannot be changed on versions lower than API 23.")
        }
    }
}