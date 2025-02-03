package com.softylasa.smartengineer.utils.helpers

import android.content.Context
import android.icu.util.Calendar
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Locale

object GetSet {

    // -------------------- Toast    ||    Snackbar ------------------------
    fun lToast(context: Context, message: String) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    fun sToast(context: Context, message: String) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    fun lSnackbar(viewGroup: ViewGroup, message: String) { Snackbar.make(viewGroup, message, Snackbar.LENGTH_LONG).show() }
    fun sSnackbar(viewGroup: ViewGroup, message: String) { Snackbar.make(viewGroup, message, Snackbar.LENGTH_SHORT).show() }


    // -------------------- Get Current Date    ||    Get Current Time    ||   Get Current Data and Time ------------------------

    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(calendar.time)
    }
    fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}