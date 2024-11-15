package com.rudhashi.smartengineer.util

import com.rudhashi.smartengineer.model.ImageUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("upload_profile_image.php")
    suspend fun uploadProfileImage(
        @Part profileImage: MultipartBody.Part,
        @Part("uid") uid: RequestBody
    ): Response<ImageUploadResponse>
}
