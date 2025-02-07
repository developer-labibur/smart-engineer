package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class Browse(
    @PropertyName("Cover") val Cover: String = "",
    @PropertyName("ID") val ID: String = "",
    @PropertyName("Des") val Des: String = "",
    @PropertyName("Name") val Name: String = ""
) {
    constructor() : this("", "", "", "") // Ensuring all fields are initialized
}