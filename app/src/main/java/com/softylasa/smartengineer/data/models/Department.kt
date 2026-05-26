package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class Department(
    @PropertyName("DAbbr") val DAbbr: String = "",
    @PropertyName("DCode") val DCode: String = "",
    @PropertyName("DIcon") val DIcon: String = "",
    @PropertyName("DInfo") val DInfo: String = "",
    @PropertyName("DName") val DName: String = ""
) {
    constructor() : this("", "", "", "", "") // Ensuring all fields are initialized
}