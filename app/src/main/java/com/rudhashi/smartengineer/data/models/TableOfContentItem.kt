package com.rudhashi.smartengineer.data.models

data class TableOfContentItem(
    val id: String,
    val title: String,
    val pdfUrl: String,
    var isDownloaded: Boolean = false
)