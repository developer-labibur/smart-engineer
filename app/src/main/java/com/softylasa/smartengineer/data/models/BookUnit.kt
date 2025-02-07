package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class BookUnit(
    @PropertyName("UName") val UName: String = "",
    @PropertyName("UNo") val UNo: String = "",
    @PropertyName("UPdf") val UPdf: String = ""
) {
    constructor() : this("", "", "") // Ensuring all fields are initialized
}