package com.softylasa.smartengineer.utils.themes

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

open class DayNightAppCompatActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var isNightMode: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the shared preferences
        sharedPreferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE)

        // Get the current theme mode
        isNightMode = sharedPreferences.getBoolean(SWITCH_BUTTON_KEY, false)
        if (isNightMode as Boolean) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    companion object {
        const val SWITCH_BUTTON_KEY = "isNightMode"
        const val PREF_KEY = "theme"
    }
}