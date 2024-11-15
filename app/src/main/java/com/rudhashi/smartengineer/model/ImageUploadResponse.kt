package com.rudhashi.smartengineer.model

data class ImageUploadResponse(
    val status: Boolean,
    val url: String?,
    val message: String?
)