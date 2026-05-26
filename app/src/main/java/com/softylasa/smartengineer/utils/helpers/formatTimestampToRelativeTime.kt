package com.softylasa.smartengineer.utils.helpers
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
