package com.rudhashi.smartengineer.data.models

import com.google.firebase.Timestamp

data class Book(
    val BCover: String = "",
    val BId: String = "",
    val BCode : String = "",
    val BName: String = "",
    val BCredit: String = "",
    val BTotalU: String = "",
    val BDes: String = "",
    val BView: Long = 0,
    val BUpdates: Timestamp? = null,
    val TimeStamp: Timestamp? = null,
    val xDep: List<String> = listOf(),  // Store as List<String> instead of List<Int>
    val xSem: List<String> = listOf()   // Store as List<String>
)