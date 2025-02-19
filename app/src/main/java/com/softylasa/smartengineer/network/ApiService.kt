package com.softylasa.smartengineer.network

import com.softylasa.smartengineer.data.models.BtebNotice
import com.softylasa.smartengineer.data.models.ImageUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
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

    @GET("notices_proxy.php")
    fun getNotices(): Call<NoticeResponse>

    // ✅ API Call to Delete Profile Picture
    @POST("delete_profile_pic.php")
    suspend fun deleteProfilePicture(@Body request: HashMap<String, String>): Response<GenericResponse>
}


//------------------------------  Data Model Class --------------------------------
// Moved `NoticeResponse` outside the interface for better organization
data class NoticeResponse(
    val notices: List<BtebNotice>
)

// Define ImageUploadResponse to handle the JSON response from the server
data class ImageUploadResponse(
    val status: Boolean,
    val url: String?, // URL of the uploaded image
    val message: String? // Error message (if any)
)

// Delete Profile Pictures from the server
data class GenericResponse(
    val success: Boolean,
    val message: String
)
