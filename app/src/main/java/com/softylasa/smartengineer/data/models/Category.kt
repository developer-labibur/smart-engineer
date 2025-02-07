package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class Category(
    @PropertyName("DAbbr") val DAbbr: String = "",
    @PropertyName("DCode") val DCode: String = "",
    @PropertyName("DCover") val DCover: String = "",
    @PropertyName("DIcon") val DIcon: String = "",
    @PropertyName("DId") val DId: Int = 0,
    @PropertyName("DInfo") val DInfo: String = "",
    @PropertyName("DName") val DName: String = ""
) {
    constructor() : this("", "", "", "", 0, "", "") // Ensuring all fields are initialized
}