package com.rudhashi.smartengineer.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object DarkThemeHelper {
    private const val PREF_NAME = "theme"
    private const val KEY_DARK_MODE = "isNightMode"

    // Save theme preference
    fun saveThemePreference(context: Context, isDarkMode: Boolean) {
        val preferences = getSharedPreferences(context)
        preferences.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
    }

    // Check if dark mode is enabled
    fun isDarkMode(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_DARK_MODE, false)
    }

    // Apply the appropriate theme
    fun applyTheme(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    // Retrieve SharedPreferences
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}