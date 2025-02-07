package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep

@Keep
data class Links(
    val Name: String = "",
    val Link: String = "",
    val ID: String = "",
    val Des: String = "",
    val Cover: String = ""
) {
    constructor() : this("", "", "", "", "") // Required for Firestore deserialization
}