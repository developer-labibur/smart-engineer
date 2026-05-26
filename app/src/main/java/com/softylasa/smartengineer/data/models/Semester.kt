package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep

@Keep
data class Semester(
    val SAbbr: String = "",
    val SDep: String = "",
    val SNo: String = "",
    val SSem: String = "",
    val STag: String = "",
    val SPdf: String = "",
    val SDone: Boolean = false
) {
    constructor() : this("", "", "", "", "", "", false) // Correct constructor for Firestore deserialization
}