package com.softylasa.smartengineer.network

import com.softylasa.smartengineer.data.models.BtebNotice
import com.softylasa.smartengineer.data.models.ImageUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
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

    // ✅ Corrected `getNotices()` method
    @GET("notices_proxy.php")
    fun getNotices(): Call<NoticeResponse>
}

// ✅ Move `NoticeResponse` outside the interface
data class NoticeResponse(
    val notices: List<BtebNotice>
)