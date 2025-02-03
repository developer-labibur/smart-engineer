package com.rudhashi.smartengineer.data.models

import com.google.firebase.Timestamp

data class Notice(
        var id: String = "",             // Document ID for the notice
        val category: String = "",        // Category for the notice
        val notice: String = "",          // Content of the notice
        val timestamp: Timestamp? = null, // Firebase Timestamp for sorting and display
        var likes: Int = 0,               // Total number of likes
        var userLiked: Boolean = false    // Indicates if the current user has liked the post
)