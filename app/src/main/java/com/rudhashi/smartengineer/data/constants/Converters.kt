package com.rudhashi.smartengineer.data.constants

import android.content.Context
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

object Converters {

    // -------------------- DP to PX    ||    PX to DP ------------------------
    fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
    fun pxToDp(context: Context, px: Float): Float {
        val density = context.resources.displayMetrics.density
        return px / density
    }
    // -------------------- Timestamp to Date  (Dec 20, 2024) ------------------------
    fun timestampToDate(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }
}