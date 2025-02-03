package com.rudhashi.smartengineer.managers

import android.content.Context
import android.content.Context.MODE_PRIVATE

object ThemeManager {
    // Shared Preferences for PDF Viewer Theme
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"

    // Retrieve selected theme mode to SharedPreferences
    fun isDarkMode(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false)
    }

    // Save selected theme mode to SharedPreferences
    fun setDarkMode(context: Context, isDarkMode: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_IS_DARK_MODE, isDarkMode).apply()
    }
}
