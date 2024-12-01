package com.rudhashi.smartengineer.util

import android.net.Uri
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rudhashi.smartengineer.model.Department
import com.rudhashi.smartengineer.model.Semester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MakeFirebaseEasy {

    companion object {
        // Get Updated Image from Server
        fun currentUserImage(pic: String): String{
            val cacheBuster = System.currentTimeMillis()
            val imagesUrl = "$pic?ver=${cacheBuster}"
            return imagesUrl
        }

        fun getAllDepartment(allDepartment: ArrayList<String>): ArrayList<String> {
            FirebaseFirestore.getInstance()
                .collection ("departments")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val xDep = document.toObject(Department::class.java)
                        val dName = xDep.DName
                        val dCode = xDep.DCode
                        val department = "$dCode => $dName"
                        allDepartment.add(department)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MakeFirebaseEasy", "Error loading units: ", exception)
                }
            return allDepartment
        }
        fun getAllDepartmentName(allDepartment: ArrayList<String>): ArrayList<String> {
            FirebaseFirestore.getInstance().collection ("departments")
                .orderBy("DCode", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val xDep = document.toObject(Department::class.java)
                        allDepartment.add(xDep.DName)
                    }
                }
            return allDepartment
        }
        fun getAllSemester(allDepartment: ArrayList<String>): ArrayList<String> {
            FirebaseFirestore.getInstance().collection ("semester").get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val xSem = document.toObject(Semester::class.java)
                        val sDep = xSem.SDep
                        val sNo = xSem.SNo
                        val semester = "$sNo => $sDep"
                        allDepartment.add(semester)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MakeFirebaseEasy", "Error loading units: ", exception)
                }
            return allDepartment
        }


        fun uploadProfileImage(selectedImage: Uri, uid: String) {
            // Prepare the file for upload
            val file = File(selectedImage.path!!)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val profileImagePart =
                MultipartBody.Part.createFormData("profile_image", "$uid.jpg", requestFile)
            val uidBody = uid.toRequestBody("text/plain".toMediaTypeOrNull())

            // Launch the image upload in a Coroutine
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response =
                        RetrofitClient.apiService.uploadProfileImage(profileImagePart, uidBody)
                    if (response.isSuccessful && response.body() != null) {
                        val userImgURL = response.body()!!.url
                        Firebase.database.reference.child("Users").child(uid).child("picture").setValue(userImgURL)
                    }
                } catch (e: Exception) {
                    Log.e("uploadProfileImage", "Error uploading image", e)
                }
            }
        }

        fun updateUserInfo(name: String?, email: String?, picture: String?, role: String?, userId: String?, joiningDate: String?, points: Int?) {
            val updatedUserInfo = hashMapOf(
                "name" to name.toString(),
                "email" to email.toString(),
                "picture" to picture.toString(),
                "role" to role.toString(),
                "userId" to userId.toString(),
                "joiningDate" to joiningDate.toString(),
                "points" to points
            )
            userId?.let { FirebaseDatabase.getInstance().reference.child("Users").child(it).updateChildren(updatedUserInfo as Map<String, Any>) }
        }

    }
}