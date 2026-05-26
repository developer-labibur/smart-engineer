package com.softylasa.smartengineer.utils.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.DialogComingSoonBinding

class SmartData {
    companion object {
        // Dark Mode Theme
        const val PREF_KEY = "theme"
        const val SWITCH_BUTTON_KEY = "isNightMode"
        // Google SignIn
        //const val WEB_CLIENT_ID = "389818419543-sdlmnl0vdps19ka0ku0n76f4vub730cr.apps.googleusercontent.com"

        // Function to display Toast
        fun lToast(context: Context, message: String) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        fun sToast(context: Context, message: String) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }

        fun Activity.setToast(text: String){
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }

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