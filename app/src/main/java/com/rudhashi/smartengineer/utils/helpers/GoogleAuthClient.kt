package com.rudhashi.smartengineer.utils.helpers

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rudhashi.smartengineer.data.constants.Constants.WEB_CLIENT_ID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    private val context: Context
) {
    private val tag = "GoogleAuthClient"
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun isSignedIn(): Boolean {
        if (firebaseAuth.currentUser != null){
            println("$tag => Already signed in")
            return true
        }
        return false
    }

    suspend fun signIn(googleSignIn: Boolean): Boolean{
        if (isSignedIn()){
            return true
        }

        try {
            val result = buildCredentialRequest(googleSignIn)
            return handleSignIn(result)
        } catch (e: Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            println("$tag => SignIn error: ${e.message}")
            return false
        }

    }

    suspend fun signOut(){
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
    }

    //===========================================
    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean{
        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ){
            try {
                Log.d(tag, "Try Block")
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                println(tag + "name = ${tokenCredential.displayName}")
                println(tag + "email = ${tokenCredential.id}")
                println(tag + "image = ${tokenCredential.profilePictureUri}")

                val authCredential = GoogleAuthProvider.getCredential(
                    tokenCredential.idToken, null
                )
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                return authResult.user!= null

            } catch (e: GoogleIdTokenParsingException) {
                println(tag + "GoogleIdTokenParsingException: ${e.message}")
                return false
            }
        } else{
            println("$tag => Credential is not GoogleIdTokenCredential")
            return false
        }
    }
    private suspend fun buildCredentialRequest(googleSignIn: Boolean): GetCredentialResponse{
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(googleSignIn)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()
        return credentialManager.getCredential(
            request = request, context = context
        )
    }
}