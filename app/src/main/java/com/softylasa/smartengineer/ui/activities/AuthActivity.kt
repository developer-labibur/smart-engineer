package com.softylasa.smartengineer.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.constants.Constants
import com.softylasa.smartengineer.utils.helpers.GetSet.lToast
import com.softylasa.smartengineer.utils.helpers.GetSet.sToast
import com.softylasa.smartengineer.data.repositories.AuthRepository
import com.softylasa.smartengineer.data.repositories.GoogleAuthClient
import com.softylasa.smartengineer.databinding.ActivityAuthBinding
import com.softylasa.smartengineer.databinding.BottomSheetLoginBinding
import com.softylasa.smartengineer.databinding.BottomSheetSignupBinding
import com.softylasa.smartengineer.managers.DialogManager
import com.softylasa.smartengineer.utils.helpers.EasySdk.setToast
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity
import kotlinx.coroutines.launch

class AuthActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private var tagLog = "AuthActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authRepository = AuthRepository()
        googleAuthClient = GoogleAuthClient(this)
        auth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        setupClickListeners()

        setTermsPrivacyText()
    }

    private fun setupClickListeners() {
        binding.btnContinueWithGoogle.setOnClickListener { showGoogleContinueBottomSheet() }
        binding.btnLogin.setOnClickListener { showLoginBottomSheet() }
        binding.btnSignup.setOnClickListener { showSignupBottomSheet() }
    }

    private fun showGoogleContinueBottomSheet() {
        lifecycleScope.launch {
            val isSignIn = googleAuthClient.signIn(false)
            if (isSignIn) {
                auth.currentUser?.let { currentUser ->
                    currentUser.reload().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navigateToDashboard()
                            setToast("Signed in successfully")
                        } else {
                            Log.e(tagLog, "Error reloading user: ${task.exception?.message}")
                            setToast("Failed to refresh user data")
                        }
                    }
                } ?: setToast("Google Sign-In failed.")
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
                        updates["name"] = "Anonymous User" // Fallback for missing name
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
                        "name" to (name ?: "Anonymous User"),
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

//    private fun showLoginBottomSheet() {
//        val dialog = BottomSheetDialog(this)
//        val bindingLogin = BottomSheetLoginBinding.inflate(layoutInflater)
//        dialog.setContentView(bindingLogin.root)
//
//        bindingLogin.apply {
//            // Focus and keyboard behavior
//            etEmail.setOnEditorActionListener { _, actionId, _ ->
//                if (actionId == EditorInfo.IME_ACTION_NEXT) {
//                    etPassword.requestFocus()
//                    true
//                } else {
//                    false
//                }
//            }
//
//            etPassword.setOnEditorActionListener { _, actionId, _ ->
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    // Call the signup function when the user presses "Done" on the keyboard
//                    validateAndLogin(bindingLogin, dialog)
//                    true
//                } else {
//                    false
//                }
//            }
//
//            // Handle Signup button click
//            btnLogin.setOnClickListener {
//                validateAndLogin(bindingLogin, dialog)
//            }
//
//            // Handle Login button click (dismiss bottom sheet)
//            btnSignUp.setOnClickListener {
//                showSignupBottomSheet()
//                dialog.dismiss() // Close the bottom sheet
//            }
//        }
//
//        dialog.show()
//    }
//
//    private fun showSignupBottomSheet() {
//        val dialog = BottomSheetDialog(this)
//        val bindingSignUp = BottomSheetSignupBinding.inflate(layoutInflater)
//        dialog.setContentView(bindingSignUp.root)
//
//        bindingSignUp.apply {
//            // Focus and keyboard behavior
//            etName.setOnEditorActionListener { _, actionId, _ ->
//                if (actionId == EditorInfo.IME_ACTION_NEXT) {
//                    etEmail.requestFocus()
//                    true
//                } else {
//                    false
//                }
//            }
//
//            etEmail.setOnEditorActionListener { _, actionId, _ ->
//                if (actionId == EditorInfo.IME_ACTION_NEXT) {
//                    etPassword.requestFocus()
//                    true
//                } else {
//                    false
//                }
//            }
//
//            etPassword.setOnEditorActionListener { _, actionId, _ ->
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    // Call the signup function when the user presses "Done" on the keyboard
//                    validateAndSignup(bindingSignUp, dialog)
//                    true
//                } else {
//                    false
//                }
//            }
//
//            // Handle Signup button click
//            btnSignupCreate.setOnClickListener {
//                validateAndSignup(bindingSignUp, dialog)
//            }
//
//            // Handle Login button click (dismiss bottom sheet)
//            btnLogin.setOnClickListener {
//                showLoginBottomSheet()
//                dialog.dismiss() // Close the bottom sheet
//            }
//        }
//
//        dialog.show()
//    }
//
//    private fun validateAndLogin(binding: BottomSheetLoginBinding, dialog: BottomSheetDialog) {
//        val email = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString().trim()
//
//        // Field validation
//        if (email.isEmpty()) {
//            binding.etEmail.error = "Email cannot be empty"
//        }
//        if (password.isEmpty()) {
//            binding.etPassword.error = "Password cannot be empty"
//        } else if (password.length < 6) {
//            binding.etPassword.error = "Password must be at least 6 characters"
//        }
//
//        // Proceed only if all fields are filled
//        if (email.isNotEmpty() && password.isNotEmpty()) {
//            try {
//                lifecycleScope.launch {
//                    Log.d(tagLog, "lifecycleScope")
//                    val isLoginSuccess = authRepository.login(email, password)
//                    if (isLoginSuccess) {
//                        sToast(this@AuthActivity, "Login successfully")
//                        startActivity(Intent(this@AuthActivity, SmartDashboardActivity::class.java))
//                        finish()
//                        dialog.dismiss()
//                    } else {
//                        sToast(this@AuthActivity, "Login Failed")
//                    }
//                }
//            } catch (e: IllegalArgumentException) {
//                lToast(this@AuthActivity, "Login Failed: $e")
//            } catch (e: Exception) {
//                lToast(this@AuthActivity, "Login Failed: $e")
//            }
//        }
//    }
//
//    private fun validateAndSignup(binding: BottomSheetSignupBinding, dialog: BottomSheetDialog) {
//        val name = binding.etName.text.toString().trim()
//        val email = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString().trim()
//
//        // Field validation
//        if (name.isEmpty()) {
//            binding.etName.error = "Name cannot be empty"
//        } else if (password.length < 3) {
//            binding.etPassword.error = "Name must be at least 3 characters"
//        }
//        if (email.isEmpty()) {
//            binding.etEmail.error = "Email cannot be empty"
//        }
//        if (password.isEmpty()) {
//            binding.etPassword.error = "Password cannot be empty"
//        } else if (password.length < 6) {
//            binding.etPassword.error = "Password must be at least 6 characters"
//        }
//
//        // Proceed only if all fields are filled
//        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
//            try {
//                lifecycleScope.launch {
//                    // Register the user via AuthRepository
//                    val isSignupSuccess = authRepository.register(name, email, password)
//                    if (isSignupSuccess) {
//                        lToast(this@AuthActivity, "SignUp Successful")
//                        showLoginBottomSheet()
//                        dialog.dismiss()
//                    } else {
//                        lToast(this@AuthActivity, "Signup failed. The email may already be in use.")
//                    }
//                }
//            } catch (e: IllegalArgumentException) {
//                lToast(this@AuthActivity, "The email address is already in use by another account.")
//            } catch (e: Exception) {
//                lToast(this@AuthActivity, "An error occurred. Please try again later.")
//            }
//        }
//    }
//
//    private fun setTermsPrivacyText(){
//        val text = "By continuing, you agree to the Terms of Service and confirm that you have read the Privacy Policy"
//        val spannableString = SpannableString(text)
//
//        // "Terms of Service" clickable span
//        val termsStart = text.indexOf("Terms of Service")
//        val termsEnd = termsStart + "Terms of Service".length
//
//        val termsClickableSpan = object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                // Open Terms and Conditions
//                openWebView("Terms of Service", "https://sites.google.com/view/smartengineer-terms-and-conditions")
//            }
//        }
//        spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        spannableString.setSpan(UnderlineSpan(), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        spannableString.setSpan(getColor(R.color.purple_text_color), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//        // "Privacy Policy" clickable span
//        val privacyStart = text.indexOf("Privacy Policy")
//        val privacyEnd = privacyStart + "Privacy Policy".length
//
//        val privacyClickableSpan = object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                // Open Privacy Policy
//                openWebView("Privacy Policy", "https://sites.google.com/view/smartengineer-privacy-policy")
//            }
//        }
//        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        spannableString.setSpan(UnderlineSpan(), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        spannableString.setSpan(getColor(R.color.purple_text_color), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//        // Set the spannable string to the TextView
//        binding.tvTermsPrivacy.text = spannableString
//        binding.tvTermsPrivacy.movementMethod = LinkMovementMethod.getInstance()
//    }
//
//    private fun openWebView(title: String, url: String) {
//        val intent = Intent(this, WebBrowserActivity::class.java)
//        intent.putExtra("TITLE", title)
//        intent.putExtra("URL", url)
//        startActivity(intent)
//    }

    private fun showLoginBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val bindingLogin = BottomSheetLoginBinding.inflate(layoutInflater)
        dialog.setContentView(bindingLogin.root)

        bindingLogin.apply {
            setupEditorActionsForLogin(this)
            btnForgotPassword.setOnClickListener { DialogManager.showForgotPasswordDialog(this@AuthActivity) }
            btnLogin.setOnClickListener { validateAndLogin(this, dialog) }
            btnSignUp.setOnClickListener {
                dialog.dismiss()
                showSignupBottomSheet()
            }
        }

        dialog.show()
    }

    private fun setupEditorActionsForLogin(bindingLogin: BottomSheetLoginBinding) {
        bindingLogin.etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                bindingLogin.etPassword.requestFocus()
                true
            } else false
        }
        bindingLogin.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateAndLogin(bindingLogin, BottomSheetDialog(this))
                true
            } else false
        }
    }

    private fun validateAndLogin(binding: BottomSheetLoginBinding, dialog: BottomSheetDialog) {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = "Email cannot be empty"
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password cannot be empty"
            return
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return
        }

        lifecycleScope.launch {
            val isLoginSuccess = authRepository.login(email, password)
            if (isLoginSuccess) {
                setToast("Login successful")
                dialog.dismiss()
                navigateToDashboard()
            } else {
                setToast("Login failed")
            }
        }
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, SmartDashboardActivity::class.java))
        finish()
    }


    private fun showSignupBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val bindingSignUp = BottomSheetSignupBinding.inflate(layoutInflater)
        dialog.setContentView(bindingSignUp.root)

        bindingSignUp.apply {
            setupEditorActionsForSignup(this)
            btnSignupCreate.setOnClickListener { validateAndSignup(this, dialog) }
            btnLogin.setOnClickListener {
                dialog.dismiss()
                showLoginBottomSheet()
            }
        }

        dialog.show()
    }

    private fun setupEditorActionsForSignup(bindingSignUp: BottomSheetSignupBinding) {
        bindingSignUp.etName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                bindingSignUp.etEmail.requestFocus()
                true
            } else false
        }
        bindingSignUp.etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                bindingSignUp.etPassword.requestFocus()
                true
            } else false
        }
        bindingSignUp.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateAndSignup(bindingSignUp, BottomSheetDialog(this))
                true
            } else false
        }
    }

    private fun validateAndSignup(binding: BottomSheetSignupBinding, dialog: BottomSheetDialog) {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name cannot be empty"
            return
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email cannot be empty"
            return
        }
        if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return
        }

        lifecycleScope.launch {
            val isSignupSuccess = authRepository.register(name, email, password)
            if (isSignupSuccess) {
                setToast("Signup successful")
                dialog.dismiss()
                showLoginBottomSheet()
            } else {
                setToast("Signup failed")
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
                // Open Terms and Conditions
                openWebView("Terms of Service", Constants.TERMS)
            }
        }
        spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(getColor(R.color.google__Blue), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // "Privacy Policy" clickable span
        val privacyStart = text.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open Privacy Policy
                openWebView("Privacy Policy", Constants.PRIVACY)
            }
        }
        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(getColor(R.color.google__Blue), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the spannable string to the TextView
        binding.tvTermsPrivacy.text = spannableString
        binding.tvTermsPrivacy.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openWebView(title: String, url: String) {
        val intent = Intent(this, WebBrowserActivity::class.java)
        intent.putExtra("TITLE", title)
        intent.putExtra("URL", url)
        startActivity(intent)
    }

}