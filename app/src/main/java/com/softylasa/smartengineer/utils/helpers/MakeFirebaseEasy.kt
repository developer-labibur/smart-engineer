package com.softylasa.smartengineer.utils.helpers

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.models.Department
import com.softylasa.smartengineer.data.models.Semester
import com.softylasa.smartengineer.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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



        fun uploadProfileImage(
            selectedImage: Uri,
            uid: String,
            onUploadSuccess: (String) -> Unit,
            useFirebaseStorage: Boolean = true
        ) {
            val firestore = FirebaseFirestore.getInstance()
            val userRef = firestore.collection("users").document(uid)
            val TAG = "ProfileImageUpload"

            if (useFirebaseStorage) {
                // Firebase Storage Upload
                val storageRef = FirebaseStorage.getInstance().reference.child("profileImages/$uid.jpg")
                storageRef.putFile(selectedImage)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()

                            // Update Firestore with the image URL
                            userRef.update("picture", imageUrl)
                                .addOnSuccessListener {
                                    Log.d(TAG, "Firestore updated successfully")
                                    onUploadSuccess(imageUrl)
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to update Firestore: ${e.message}", e)
                                }
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Failed to retrieve download URL: ${e.message}", e)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to upload image: ${e.message}", e)
                    }
            } else {
                // External Server Upload via Retrofit
                val file = File(selectedImage.path ?: return) // Prevent crash if path is null
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val profileImagePart = MultipartBody.Part.createFormData("profile_image", "$uid.jpg", requestFile)
                val uidBody = uid.toRequestBody("text/plain".toMediaTypeOrNull())

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.apiService.uploadProfileImage(profileImagePart, uidBody)
                        if (response.isSuccessful && response.body() != null) {
                            val userImgURL = response.body()?.url

                            // ✅ Ensure `userImgURL` is not null or empty
                            if (!userImgURL.isNullOrEmpty()) {
                                withContext(Dispatchers.Main) {
                                    userRef.update("picture", userImgURL)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Firestore updated successfully")
                                            onUploadSuccess(userImgURL)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Failed to update Firestore: ${e.message}", e)
                                        }
                                }
                            } else {
                                Log.e(TAG, "Error: Received null or empty image URL from server")
                            }
                        } else {
                            Log.e(TAG, "Server error: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error uploading image: ${e.message}", e)
                    }
                }
            }
        }


        fun updateUserInfo(name: String?, email: String?, picture: String?, role: String?, userId: String?, joiningDate: String?, points: Int?, pointsAll: Int?) {
            val updatedUserInfo = hashMapOf(
                "name" to name.toString(),
                "email" to email.toString(),
                "picture" to picture.toString(),
                "role" to role.toString(),
                "userId" to userId.toString(),
                "joiningDate" to joiningDate.toString(),
                "points" to points,
                "pointsAll" to pointsAll
            )
            userId?.let { FirebaseDatabase.getInstance().reference.child("Users").child(it).updateChildren(updatedUserInfo as Map<String, Any>) }
        }

        fun setBookFavorites(view: ImageView, bCode: String, bName: String, bCover: String){
            val userID = FirebaseAuth.getInstance().currentUser?.uid
            if (userID != null) {
                val databaseRef = FirebaseDatabase.getInstance().getReference("UsersDB/$userID/Favourites/Books")

                // Attach ValueEventListener ONCE
                databaseRef.child(bCode).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Book is in favorites
                            view.setImageResource(R.drawable.sl_heart_red)
                        } else {
                            // Book is not in favorites
                            view.setImageResource(R.drawable.sl_heart_not_fill)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        SmartData.sToast(view.context, "Error accessing favorites")
                    }
                })

                // Handle Favorite Button Click
                view.setOnClickListener {
                    databaseRef.child(bCode).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (task.result.exists()) {
                                // Remove from favorites
                                databaseRef.child(bCode).removeValue()
                                    .addOnSuccessListener {
                                        SmartData.sToast(view.context, "Removed from Favourites")
                                    }
                            } else {
                                // Add to favorites
                                val bookData = mapOf(
                                    "BName" to bName,
                                    "BCode" to bCode,
                                    "BIcon" to bCover
                                )
                                databaseRef.child(bCode).setValue(bookData)
                                    .addOnSuccessListener {
                                        SmartData.sToast(view.context, "Added to Favourites")
                                    }
                            }
                        } else {
                            SmartData.sToast(view.context, "Error accessing favorites")
                        }
                    }
                }
            } else {
                SmartData.sToast(view.context, "User not logged in")
            }
        }

    }
}