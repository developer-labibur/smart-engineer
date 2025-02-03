package com.softylasa.smartengineer.managers

import android.content.Context
import android.content.Context.MODE_PRIVATE

object AnimationManager {

    // Shared Preferences for PDF Viewer Animation
    private const val PREF_NAME = "animation_prefs"
    private const val KEY_ANIMATION_MODE = "animation_mode"

    // Save selected animation mode to SharedPreferences
    fun saveAnimationMode(mode: Int, context: Context) {
        val preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        preferences.edit().putInt(KEY_ANIMATION_MODE, mode).apply()
    }

    // Retrieve saved animation mode from SharedPreferences
    fun getAnimationMode(context: Context): Int {
        val preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        return preferences.getInt(KEY_ANIMATION_MODE, 5) // Default to "Shrink and Expand Animation"
    }

}
