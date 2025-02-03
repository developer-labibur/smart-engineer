package com.softylasa.smartengineer.data.models

data class ImageUploadResponse(
    val status: Boolean,
    val url: String?,
    val message: String?
)