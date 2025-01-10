package com.rudhashi.smartengineer.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.dashboard.SmartDashboardActivity
import com.rudhashi.smartengineer.data.repositories.AuthRepository
import com.rudhashi.smartengineer.data.repositories.GoogleAuthClient
import com.rudhashi.smartengineer.databinding.ActivityAuthBinding
import com.rudhashi.smartengineer.databinding.BottomSheetLoginBinding
import com.rudhashi.smartengineer.databinding.BottomSheetSignupBinding
import com.rudhashi.smartengineer.model.User
import com.rudhashi.smartengineer.page.AboutUsActivity
import com.rudhashi.smartengineer.util.MakeFirebaseEasy
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.view.DashboardActivity
import kotlinx.coroutines.launch
import kotlin.coroutines.resume

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private var tagLog = "AuthActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository()
        googleAuthClient = GoogleAuthClient(this)
        auth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.btnContinueWithGoogle.setOnClickListener {
            showGoogleContinueBottomSheet()
        }
        binding.btnLogin.setOnClickListener {
            showLoginBottomSheet()
        }
        binding.btnSignup.setOnClickListener {
            showSignupBottomSheet()
        }

        setTermsPrivacyText()
    }

    private fun showGoogleContinueBottomSheet() {
        lifecycleScope.launch {
            val isSignIn = googleAuthClient.signIn(false)
            if (isSignIn) {
                val userId = auth.currentUser?.uid
                val email = auth.currentUser?.email
                val name = auth.currentUser?.displayName
                val picture = auth.currentUser?.photoUrl.toString()

                if (userId != null) {
                    handleFirestoreUserData(userId, name, email, picture)
                    val intent = Intent(this@AuthActivity, SmartDashboardActivity::class.java)
                    startActivity(intent)
                    finish()

                    SmartData.sToast(this@AuthActivity, "Sign In Successfully")
                    Log.i(tagLog, "showGoogleContinueBottomSheet: User: $userId")
                }
            }
        }
    }

    private fun handleFirestoreUserData(uid: String, name: String?, email: String?, picture: String?) {
        val userRef = firebaseFirestore.collection("users").document(uid)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    // Document exists, check for missing fields
                    val updates = mutableMapOf<String, Any>()

                    if (document.getString("name").isNullOrEmpty() && !name.isNullOrEmpty()) {
                        updates["name"] = name
                    } else if (document.getString("name").isNullOrEmpty()) {
                        updates["name"] = "Unknown User" // Fallback for missing name
                    }

                    if (document.getString("email").isNullOrEmpty() && !email.isNullOrEmpty()) {
                        updates["email"] = email
                    } else if (document.getString("email").isNullOrEmpty()) {
                        updates["email"] = "No Email Provided" // Fallback for missing email
                    }

                    if (document.getString("picture").isNullOrEmpty() && !picture.isNullOrEmpty()) {
                        updates["picture"] = picture
                    } else if (document.getString("picture").isNullOrEmpty()) {
                        updates["picture"] = "" // Fallback for missing pictures
                    }

                    if (updates.isNotEmpty()) {
                        userRef.update(updates).addOnSuccessListener {
                            Log.d(tagLog, "Firestore fields updated: $updates")
                        }.addOnFailureListener { e ->
                            Log.e(tagLog, "Error updating Firestore: ${e.message}")
                        }
                    } else {
                        Log.d(tagLog, "No updates needed for Firestore document.")
                    }
                } else {
                    // Document does not exist, create a new one
                    val userData = hashMapOf(
                        "name" to (name ?: "Unknown User"),
                        "email" to (email ?: "No Email Provided"),
                        "picture" to (picture ?: ""),
                        "uid" to uid
                    )
                    userRef.set(userData).addOnSuccessListener {
                        Log.d(tagLog, "New Firestore document created: $userData")
                    }.addOnFailureListener { e ->
                        Log.e(tagLog, "Error creating Firestore document: ${e.message}")
                    }
                }
            } else {
                Log.e(tagLog, "Error fetching Firestore document: ${task.exception?.message}")
            }
        }
    }

    private fun showLoginBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val bindingLogin = BottomSheetLoginBinding.inflate(layoutInflater)
        dialog.setContentView(bindingLogin.root)

        bindingLogin.apply {
            // Focus and keyboard behavior
            etEmail.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    etPassword.requestFocus()
                    true
                } else {
                    false
                }
            }

            etPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Call the signup function when the user presses "Done" on the keyboard
                    validateAndLogin(bindingLogin, dialog)
                    true
                } else {
                    false
                }
            }

            // Handle Signup button click
            btnLogin.setOnClickListener {
                validateAndLogin(bindingLogin, dialog)
            }

            // Handle Login button click (dismiss bottom sheet)
            btnSignUp.setOnClickListener {
                showSignupBottomSheet()
                dialog.dismiss() // Close the bottom sheet
            }
        }

        dialog.show()
    }

    private fun showSignupBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val bindingSignUp = BottomSheetSignupBinding.inflate(layoutInflater)
        dialog.setContentView(bindingSignUp.root)

        bindingSignUp.apply {
            // Focus and keyboard behavior
            etName.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    etEmail.requestFocus()
                    true
                } else {
                    false
                }
            }

            etEmail.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    etPassword.requestFocus()
                    true
                } else {
                    false
                }
            }

            etPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Call the signup function when the user presses "Done" on the keyboard
                    validateAndSignup(bindingSignUp, dialog)
                    true
                } else {
                    false
                }
            }

            // Handle Signup button click
            btnSignupCreate.setOnClickListener {
                validateAndSignup(bindingSignUp, dialog)
            }

            // Handle Login button click (dismiss bottom sheet)
            btnLogin.setOnClickListener {
                showLoginBottomSheet()
                dialog.dismiss() // Close the bottom sheet
            }
        }

        dialog.show()
    }

    private fun validateAndLogin(binding: BottomSheetLoginBinding, dialog: BottomSheetDialog) {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Field validation
        if (email.isEmpty()) {
            binding.etEmail.error = "Email cannot be empty"
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password cannot be empty"
        } else if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
        }

        // Proceed only if all fields are filled
        if (email.isNotEmpty() && password.isNotEmpty()) {
            try {
                lifecycleScope.launch {
                    Log.d(tagLog, "lifecycleScope")
                    val isLoginSuccess = authRepository.login(email, password)
                    if (isLoginSuccess) {
                        SmartData.sToast(this@AuthActivity, "Login successfully")
                        startActivity(Intent(this@AuthActivity, SmartDashboardActivity::class.java))
                        finish()
                        dialog.dismiss()
                    } else {
                        SmartData.sToast(this@AuthActivity, "Login Failed")
                    }
                }
            } catch (e: IllegalArgumentException) {
                SmartData.lToast(this@AuthActivity, "Login Failed: $e")
            } catch (e: Exception) {
                SmartData.lToast(this@AuthActivity, "Login Failed: $e")
            }
        }
    }

    private fun validateAndSignup(binding: BottomSheetSignupBinding, dialog: BottomSheetDialog) {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Field validation
        if (name.isEmpty()) {
            binding.etName.error = "Name cannot be empty"
        } else if (password.length < 3) {
            binding.etPassword.error = "Name must be at least 3 characters"
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email cannot be empty"
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password cannot be empty"
        } else if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
        }

        // Proceed only if all fields are filled
        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            try {
                lifecycleScope.launch {
                    // Register the user via AuthRepository
                    val isSignupSuccess = authRepository.register(name, email, password)
                    if (isSignupSuccess) {
                        SmartData.lToast(this@AuthActivity, "SignUp Successful")
                        showLoginBottomSheet()
                        dialog.dismiss()
                    } else {
                        SmartData.lToast(
                            this@AuthActivity,
                            "Signup failed. The email may already be in use."
                        )
                    }
                }
            } catch (e: IllegalArgumentException) {
                SmartData.lToast(
                    this@AuthActivity,
                    "The email address is already in use by another account."
                )
            } catch (e: Exception) {
                SmartData.lToast(this@AuthActivity, "An error occurred. Please try again later.")
            }
        }
    }

    private fun setTermsPrivacyText(){
        val text = "By continuing, you agree to the Terms of Service and confirm that you have read the Privacy Policy"
        val spannableString = SpannableString(text)

        // "Terms of Service" clickable span
        val termsStart = text.indexOf("Terms of Service")
        val termsEnd = termsStart + "Terms of Service".length

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle Terms of Service click
                val intent = Intent(this@AuthActivity, AboutUsActivity::class.java)
                startActivity(intent)
            }
        }
        spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // "Privacy Policy" clickable span
        val privacyStart = text.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle Privacy Policy click
                val intent = Intent(this@AuthActivity, AboutUsActivity::class.java)
                startActivity(intent)
            }
        }
        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the spannable string to the TextView
        binding.tvTermsPrivacy.text = spannableString
        binding.tvTermsPrivacy.movementMethod = LinkMovementMethod.getInstance()
    }

}