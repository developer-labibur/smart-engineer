package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep

@Keep
data class SemesterTotal(
    val STName: String = "",
    val STImage: String = ""
) {
    constructor() : this("", "") // Required for Firestore deserialization
}