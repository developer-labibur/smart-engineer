package com.rudhashi.smartengineer.util

import android.content.Context
import android.widget.Toast
import org.checkerframework.checker.units.qual.Length

class SmartData {
    companion object {
        // Dark Mode Theme
        const val PREF_KEY = "theme"
        const val SWITCH_BUTTON_KEY = "isNightMode"
        // Splash Screen
        const val SPLASH_SCREEN: Long = 2000
        // Google SignIn
        const val WEB_CLIENT_ID = "389818419543-sdlmnl0vdps19ka0ku0n76f4vub730cr.apps.googleusercontent.com"

        // Function to display Toast
        fun lToast(context: Context, message: String, isShort: Boolean) {
            if (isShort){
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }

    }
}