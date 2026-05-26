package com.softylasa.smartengineer.utils.helpers

import android.content.Context
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

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

    // -------------------- Timestamp to Real Time  || Dec 20, 2024 || Just Now || 7 min ago ------------------------
    fun formatTimestampToRelativeTime(timestamp: Timestamp): String {
        val currentTime = System.currentTimeMillis()
        val postTime = timestamp.toDate().time
        val timeDifference = currentTime - postTime

        return when {
            timeDifference < TimeUnit.MINUTES.toMillis(1) -> "Just Now"
            timeDifference < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(timeDifference)} min ago"
            timeDifference < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(timeDifference)} hours ago"
            timeDifference < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(timeDifference)} days ago"
            else -> {
                // If older than a week, show as "Sep 27, 2024" format
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(timestamp.toDate())
            }
        }
    }

    // -------------------- Department Name to Department Code ------------------------
    fun covertDepartmentToCode(name: Any): String {
        val codeStr = name.toString()  // Convert any type to string for matching
        return when (codeStr) {
            "Architecture Technology" -> "61"
            "Civil Technology" -> "64"
            "Computer Science and Technology" -> "85"
            "Electrical Technology" -> "67"
            "Electronics Technology" -> "68"
            "Food Technology" -> "69"
            "Mechanical Technology" -> "70"
            "Construction Technology" -> "88"
            "Power Technology" -> "71"
            "BSC Engineering" -> "101"
            "Other" -> "102"
            else -> ""
        }
    }

    // -------------------- Department Code to Department Name ------------------------
    fun covertDepCodeToName(code: Any): String {
        val codeStr = code.toString()  // Convert any type to string for matching
        return when (codeStr) {
            "61" -> "Architecture Technology"
            "64" -> "Civil Technology"
            "85" -> "Computer Science and Technology"
            "67" -> "Electrical Technology"
            "68" -> "Electronics Technology"
            "69" -> "Food Technology"
            "70" -> "Mechanical Technology"
            "88" -> "Construction Technology"
            "71" -> "Power Technology"
            else -> "Unknown Department"
        }
    }

    // -------------------- Department Name to Department Code ------------------------
    fun covertDepartmentToAbbr(name: Any): String {
        val codeStr = name.toString()  // Convert any type to string for matching
        return when (codeStr) {
            "Architecture Technology" -> "AIDT"
            "Civil Technology" -> "CT"
            "Computer Science and Technology" -> "CST"
            "Electrical Technology" -> "ET"
            "Electronics Technology" -> "ENT"
            "Food Technology" -> "FT"
            "Mechanical Technology" -> "MT"
            "Construction Technology" -> "CNT"
            "Power Technology" -> "PT"
            "BSC Engineering" -> "BSC"
            "Other" -> "N/A"
            else -> ""
        }
    }

    // -------------------- Semester Name to Semester Code ------------------------
    fun covertSemNameToCode(name: Any): String {
        val codeStr = name.toString()  // Convert any type to string for matching
        return when (codeStr) {
            "1st Semester" -> "1"
            "2nd Semester" -> "2"
            "3rd Semester" -> "3"
            "4th Semester" -> "4"
            "5th Semester" -> "5"
            "6th Semester" -> "6"
            "7th Semester" -> "7"
            "8th Semester" -> "8"
            else -> ""
        }
    }
}