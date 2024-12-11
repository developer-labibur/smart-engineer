package com.rudhashi.smartengineer.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    fun saveThemePreference(context: Context, isDarkMode: Boolean) {
        val sharedPreferences = context.getSharedPreferences(SmartData.PREF_KEY, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(SmartData.SWITCH_BUTTON_KEY, isDarkMode)
            apply()
        }
    }

    fun isDarkMode(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(SmartData.PREF_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)
    }

    fun applyTheme(isDarkModeOn: Boolean) {
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
