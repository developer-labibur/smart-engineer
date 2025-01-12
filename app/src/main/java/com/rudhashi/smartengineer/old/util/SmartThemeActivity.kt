@file:Suppress("DEPRECATION")

package com.rudhashi.smartengineer.old.util

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.rudhashi.smartengineer.manager.CUserManager

open class SmartThemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        CUserManager.initialize()
    }

    private fun applyTheme() {
        // Load the theme preference
        val sharedPreferences = getSharedPreferences(SmartData.PREF_KEY, MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)

        // Apply the theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            setSystemUI(isDark = true)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            setSystemUI(isDark = false)
        }
    }

    private fun setSystemUI(isDark: Boolean) {
        // Set status bar text color and background
        window.decorView.systemUiVisibility = if (isDark) {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        window.statusBarColor = Color.TRANSPARENT // Transparent background for status bar

        // Set navigation bar background dynamically
        val backgroundColor = getBackgroundColorFromTheme()
        window.navigationBarColor = backgroundColor

        // Adjust navigation bar button colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = if (isDark) {
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            } else {
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
    }
    private fun getBackgroundColorFromTheme(): Int {
        // Retrieve the activity's background color from the current theme
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        return if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // If it's a color, return it
            typedValue.data
        } else {
            // Fallback color (e.g., white)
            Color.WHITE
        }
    }
}