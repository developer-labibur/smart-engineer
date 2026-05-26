package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep

@Keep
data class User(
    val email: String = "",
    val department: String = "",
    val institute: String = "",
    val lastBonusDate: String = "",
    val name: String = "",
    val picture: String = "",
    val points: Int = 0,
    val pointsAll: Int = 0,
    val role: String = "",
    val semester: String = "",
    val userId: String = "",
    val joiningDate: String = ""
) {
    constructor() : this("","","","","","", 0 ,0,"", "", "","")
// Required for Firestore deserialization
}