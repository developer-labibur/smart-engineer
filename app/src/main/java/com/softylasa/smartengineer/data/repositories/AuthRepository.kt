package com.softylasa.smartengineer.data.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepository {

    private val tag = "AuthRepository"
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun register(name: String, email: String, password: String): Boolean {
        return suspendCoroutine { continuation ->
            var isResumed = false  // Flag to track if the continuation has been resumed
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        user?.let {
                            val userId = it.uid
                            val userData = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "uid" to userId
                            )

                            // Save user to Firestore
                            firebaseFirestore.collection("users")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    if (!isResumed) {
                                        continuation.resume(true)  // Only resume once
                                        isResumed = true
                                    }
                                }
                                .addOnFailureListener {
                                    if (!isResumed) {
                                        continuation.resume(false)  // Only resume once
                                        isResumed = true
                                    }
                                }
                        } ?: run {
                            if (!isResumed) {
                                continuation.resume(false)  // Handle case if user is null
                                isResumed = true
                            }
                        }
                    } else {
                        val errorMessage = task.exception?.message
                        Log.e(tag, "Firebase registration failed: $errorMessage")
                        if (!isResumed) {
                            continuation.resume(false)  // Only resume once
                            isResumed = true
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Error: ${exception.message}")
                    if (!isResumed) {
                        continuation.resume(false)  // Only resume once
                        isResumed = true
                    }
                }
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return suspendCoroutine { continuation ->
            var isResumed = false  // Flag to track if the continuation has been resumed
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    if (!isResumed) {
                        continuation.resume(true)  // Only resume once
                        isResumed = true
                    }
                }
                .addOnFailureListener {
                    if (!isResumed) {
                        continuation.resume(false)  // Only resume once
                        isResumed = true
                    }
                }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}