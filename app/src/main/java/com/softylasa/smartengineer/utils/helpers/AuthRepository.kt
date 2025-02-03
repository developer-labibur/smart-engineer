package com.softylasa.smartengineer.utils.helpers

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.softylasa.smartengineer.utils.helpers.GetSet.getCurrentDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepository {

    private val tag = "AuthRepository"

    // Initialize Firebase Auth and Firebase Database
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun isLoggedIn(): Boolean {
        if (firebaseAuth.currentUser != null) {
            Log.d(tag, "Already logged in")
            return true
        }

        return false
    }

    suspend fun register(name: String, email: String, password: String): Boolean {
        try {
            val result = suspendCoroutine { continuation ->
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            CoroutineScope(Dispatchers.IO).launch {
                                Log.e(tag, "register success")

                                val id = task.result.user!!.uid
                                //val userInfo = Users(id, name, "", email, "User")
                                //database.reference.child("Users").child(id).setValue(userInfo)

                                MakeFirebaseEasy.updateUserInfo(
                                    name,
                                    email,
                                    "",
                                    "User",
                                    id,
                                    getCurrentDate(),
                                    SmartConfig.NEW_USER_SIGNUP_BONUS_POINT,
                                    SmartConfig.NEW_USER_SIGNUP_BONUS_POINT
                                )
                                continuation.resume(login(email = email, password = password))
                            }
                        } else{
                            continuation.resume(false)
                            Log.e(tag, "register failure")
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(false)
                        Log.e(tag, "register failure")
                    }
            }

            return result
        } catch (e: Exception) {
            Log.e(tag, e.printStackTrace().toString())
            if (e is CancellationException) throw e
            Log.e(tag, "register exception ${e.message}")
            return false
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        try {
            Log.d(tag, "Enter Try Block")
            val result = suspendCoroutine { continuation ->
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Log.e(tag, "Login Successful")
                        continuation.resume(true)
                    }
                    .addOnFailureListener {
                        Log.e(tag, "Login Failure")
                        continuation.resume(false)
                    }
            }
            return result
        } catch (e: Exception) {
            Log.d(tag, "Enter Catch Block")
            Log.e(tag, e.printStackTrace().toString())
            if (e is CancellationException) throw e
            Log.e(tag, "login exception ${e.message}")
            return false
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

}