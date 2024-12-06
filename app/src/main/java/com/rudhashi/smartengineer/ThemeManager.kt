package com.rudhashi.smartengineer

import android.content.Context
import android.content.Context.MODE_PRIVATE

object ThemeManager {
    // Shared Preferences for PDF Viewer Theme
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"

    // Shared Preferences for PDF Viewer Animation
    private const val PREF_NAME = "animation_prefs"
    private const val KEY_ANIMATION_MODE = "animation_mode"

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

    // Save selected animation mode to SharedPreferences
    fun saveAnimationMode(mode: Int, context: Context) {
        val preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        preferences.edit().putInt(KEY_ANIMATION_MODE, mode).apply()
    }

    // Retrieve saved animation mode from SharedPreferences
    fun getAnimationMode(context: Context): Int {
        val preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        return preferences.getInt(KEY_ANIMATION_MODE, 1) // Default to "Shrink and Expand Animation"
    }


}
