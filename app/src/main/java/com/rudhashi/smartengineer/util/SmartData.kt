package com.rudhashi.smartengineer.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.DialogComingSoonBinding
import java.text.SimpleDateFormat
import java.util.Locale

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
        fun lToast(context: Context, message: String) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        fun sToast(context: Context, message: String) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        fun lSnackbar(viewGroup: ViewGroup, message: String) { Snackbar.make(viewGroup, message,Snackbar.LENGTH_LONG).show() }
        fun sSnackbar(viewGroup: ViewGroup, message: String) { Snackbar.make(viewGroup, message,Snackbar.LENGTH_SHORT).show() }

        @SuppressLint("InflateParams")
        fun comingSoon(context: Context){
            val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_coming_soon, null)
            val dialogBinding: DialogComingSoonBinding = DialogComingSoonBinding.bind(dialogView)
            builder.setView(dialogBinding.root)
            builder.show()

            dialogBinding.btnOk.setOnClickListener { builder.dismiss() }

            if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
            builder.show()
        }
        fun timestampToDate(timestamp: Timestamp): String {
            // Convert Timestamp to a Date object
            val date = timestamp.toDate()

            // Format Date to "Dec 20, 2024" format
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return dateFormat.format(date)
        }
        fun getCurrentDate(): String {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return dateFormat.format(calendar.time)
        }
        fun dpToPx(context: Context, dp: Float): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }
        fun pxToDp(context: Context, px: Float): Float {
            val density = context.resources.displayMetrics.density
            return px / density
        }
        fun randomNumber(int: Int): Int { return (Math.random() * int).toInt() }

        fun RecyclerView.setHeightBasedOnItems(doubleHeight: Int) {
            val adapter = this.adapter ?: return
            val totalItems = adapter.itemCount
            val itemHeight = doubleHeight // Approximate height of one item in pixels
            val totalHeight = totalItems * itemHeight
            val layoutParams = this.layoutParams
            layoutParams.height = totalHeight
            this.layoutParams = layoutParams
        }
    }
}