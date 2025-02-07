package com.softylasa.smartengineer.data.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp

@Keep
data class Book(
    val BCover: String = "",
    val BId: String = "",
    val BCode: String = "",
    val BName: String = "",
    val BCredit: String = "",
    val BTotalU: String = "",
    val BDes: String = "",
    val BView: Long = 0,
    val BUpdates: Timestamp? = null,
    val TimeStamp: Timestamp? = null,
    val xDep: List<String> = listOf(),  // Store as List<String>
    val xSem: List<String> = listOf()   // Store as List<String>
) {
    constructor() : this("", "", "", "", "", "", "", 0, null, null, listOf(), listOf())
}