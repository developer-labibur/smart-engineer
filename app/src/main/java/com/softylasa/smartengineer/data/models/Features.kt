package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep
import com.google.firebase.database.PropertyName

@Keep
data class Features(
    @PropertyName("dataId") val dataId: String = "",
    @PropertyName("dataTitle") val dataTitle: String = "",
    @PropertyName("dataImageUrl") val dataImageUrl: String = ""
) {
    constructor() : this("", "", "") // Required for Firestore deserialization
}