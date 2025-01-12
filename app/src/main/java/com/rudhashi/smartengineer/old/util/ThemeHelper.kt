package com.rudhashi.smartengineer.old.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    // Save theme preference
    fun saveThemePreference(context: Context, isDarkMode: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences(SmartData.PREF_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(SmartData.SWITCH_BUTTON_KEY, isDarkMode).apply()
    }

    // Check if dark mode is enabled
    fun isDarkMode(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(SmartData.PREF_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)
    }

    // Apply the theme
    fun applyTheme(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    // Get shared preferences
    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SmartData.PREF_KEY, Context.MODE_PRIVATE)
    }
}
