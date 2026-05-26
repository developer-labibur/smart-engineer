package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep
import com.google.firebase.database.PropertyName

@Keep
data class Favorite(
    @PropertyName("BCode") val BCode: String = "",
    @PropertyName("BName") val BName: String = "",
    @PropertyName("BCover") val BCover: String = ""
) {
    constructor() : this("", "", "") // Ensuring all fields are initialized
}