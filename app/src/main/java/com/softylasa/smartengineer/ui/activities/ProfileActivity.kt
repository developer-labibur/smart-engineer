package com.softylasa.smartengineer.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.utils.helpers.Converters.dpToPx
import com.softylasa.smartengineer.databinding.ActivityProfileBinding
import com.softylasa.smartengineer.data.models.User
import com.softylasa.smartengineer.data.repositories.AuthRepository
import com.softylasa.smartengineer.databinding.DialogChangePictureBinding
import com.softylasa.smartengineer.databinding.DialogChangeUsernameBinding
import com.softylasa.smartengineer.managers.CUserManager
import com.softylasa.smartengineer.managers.DialogManager.showNegativeInfoDialog
import com.softylasa.smartengineer.managers.DialogManager.showUploadingDialog
import com.softylasa.smartengineer.utils.helpers.EasySdk.setToast
import com.softylasa.smartengineer.utils.helpers.MakeFirebaseEasy
import com.softylasa.smartengineer.utils.helpers.MakeFirebaseEasy.Companion.currentUserImage
import com.softylasa.smartengineer.utils.helpers.MakeFirebaseEasy.Companion.uploadProfileImage


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    private lateinit var dialogPictureBinding: DialogChangePictureBinding
    private lateinit var dialogBinding: DialogChangeUsernameBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var fbDB: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var selectedImage: Uri
    private val tagLog = "ProfileActivity"

    private val firestore: FirebaseFirestore = Firebase.firestore


    private val pictureListener: (String) -> Unit = {
        if (it.isEmpty()) {
            binding.proPic.setImageResource(R.drawable.avatar)
        } else {
            if (!this.isDestroyed && !this.isFinishing) {
                Glide.with(this)
                    .load(MakeFirebaseEasy.currentUserImage(it))
                    .placeholder(R.drawable.avatar)
                    .transform(RoundedCorners(dpToPx(this, 50f)))
                    .into(binding.proPic)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        fbDB = Firebase.database.reference
        currentUser = auth.currentUser!!

        binding.apply {
            backProfileBtn.setOnClickListener { finish() }
            editPro.setOnClickListener { setProfilePictureInfo() }
            backLogout.setOnClickListener {
                showNegativeInfoDialog(
                    this@ProfileActivity,
                    R.raw.anim_logout,
                    "Logout",
                    "Are you sure you want to logout?"
                ) { setLogOut() }
            }
            btnFavourite.setOnClickListener {
                val intent = Intent(this@ProfileActivity, BookViewActivity::class.java)
                intent.putExtra("DISPLAY", "Favourite")
                startActivity(intent)
            }
            btnLibrary.setOnClickListener {
                val intent = Intent(this@ProfileActivity, BookViewActivity::class.java)
                intent.putExtra("DISPLAY", "Library")
                startActivity(intent)
            }
            btnSettings.setOnClickListener {
                val intent = Intent(this@ProfileActivity, SmartDashboardActivity::class.java)
                intent.putExtra("DISPLAY", "Setting")
                startActivity(intent)
            }
            proBtnNameEdit.setOnClickListener { setProfileUsername() }
            proBtnEdit.setOnClickListener {
                startActivity(
                    Intent(
                        this@ProfileActivity,
                        UpdateProfileActivity::class.java
                    )
                )
            }
            proBtnDelete.setOnClickListener {
                showNegativeInfoDialog(
                    this@ProfileActivity,
                    R.raw.anim_delete,
                    "Delete Account",
                    "Are you sure you want to delete your account? This action cannot be undone."
                ) { deleteUserAccount() }
            }

        }

        loadProfileInfo()
    }

    private fun loadProfileInfo() {
        CUserManager.initialize()
        CUserManager.apply {
            name { binding.proName.text = it }
            email { binding.proEmail.text = it }
            department { binding.proDepartment.text = it }
            institute { binding.proInstitute.text = it }
            semester { binding.proSemester.text = it }
            picture(pictureListener)
        }
        setUserStatus("Online") // Mark user as online
    }

    // == == == == == == == == == == ==  Edit Change Picture Functionality  == == == == == == == == == == == == == ==
    @SuppressLint("InflateParams")
    private fun setProfilePictureInfo() {
        val builder = AlertDialog.Builder(this@ProfileActivity).create()
        val dialogView =
            LayoutInflater.from(this@ProfileActivity).inflate(R.layout.dialog_change_picture, null)
        dialogPictureBinding = DialogChangePictureBinding.bind(dialogView)
        builder.setView(dialogPictureBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogPictureBinding.apply {

            // Fetch and display user data
            val userDocRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)

            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    Glide.with(this@ProfileActivity)
                        .load(user?.picture?.let { currentUserImage(it) })
                        .placeholder(R.drawable.avatar)
                        .into(proPic)
                }
            }.addOnFailureListener {
                Toast.makeText(this@ProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT)
                    .show()
            }

            // Update Profile Picture
            dialogPictureBinding.btnUpdate.setOnClickListener {
                if (this@ProfileActivity::selectedImage.isInitialized) {
                    // ✅ Show Progress Bar Before Upload Starts
                    val progressDialog = showUploadingDialog(this@ProfileActivity)

                    // Step 1: Upload Image to Web Server or Firebase Storage
                    uploadProfileImage(
                        selectedImage,
                        currentUser.uid,
                        { webServerUrl ->
                            // Step 2: Update Firestore with the URL
                            val updatedUserInfo = mapOf("picture" to webServerUrl)
                            firestore.collection("users").document(currentUser.uid)
                                .update(updatedUserInfo)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@ProfileActivity,
                                        "Profile updated successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    builder.dismiss()  // Close the image selection dialog
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this@ProfileActivity,
                                        "Error updating profile: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnCompleteListener {
                                    // ✅ Hide Progress Bar When Upload is Done (Success/Failure)
                                    progressDialog.dismiss()
                                }
                        },
                        useFirebaseStorage = false
                    )
                    builder.dismiss()
                } else {
                    Toast.makeText(this@ProfileActivity, "No image selected", Toast.LENGTH_SHORT).show()
                }
            }

            dialogPictureBinding.btnCancel.setOnClickListener { builder.dismiss() }
            dialogPictureBinding.editPro.setOnClickListener { callImagePicker() }
            dialogPictureBinding.proCamera.setOnClickListener { callImagePicker() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }


    // == == == == == == == == == == ==  Edit Profile Username button Functionality  == == == == == == == == == == == == == ==
    @SuppressLint("InflateParams")
    private fun setProfileUsername() {
        val builder = AlertDialog.Builder(this@ProfileActivity).create()
        val dialogView =
            LayoutInflater.from(this@ProfileActivity).inflate(R.layout.dialog_change_username, null)
        dialogBinding = DialogChangeUsernameBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.apply {

            // Fetch and display user data
            val userDocRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)

            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    dialogBinding.editProName.setText(user?.name)
                }
            }.addOnFailureListener {
                setToast("Failed to load user data")
            }

            dialogBinding.btnSave.setOnClickListener {
                builder.dismiss()
                val newName = dialogBinding.editProName.text.toString()
                val updatedUserInfo = mapOf("name" to newName)

                if (newName.isEmpty()) {
                    dialogBinding.editProName.error = "Please enter a valid username"
                    return@setOnClickListener
                } else {
                    firestore.collection("users").document(currentUser.uid)
                        .update(updatedUserInfo)
                        .addOnSuccessListener {
                            setToast("Username updated successfully!")
                            builder.dismiss()
                        }.addOnFailureListener { e ->
                            setToast("Error updating profile: ${e.message}")
                        }
                }
            }

            dialogBinding.btnCancel.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    private fun callImagePicker() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent -> startForProfileImageResult.launch(intent) }
    }


    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val uri = result.data?.data
                    uri?.let {
                        binding.proPic.setImageURI(it)
                        dialogPictureBinding.proPic.setImageURI(it)
                        selectedImage = it
                        setToast("Image Selected")
                    }
                }

                ImagePicker.RESULT_ERROR -> setToast(ImagePicker.getError(result.data))

                else -> setToast("Not Select Any Image")
            }
        }

    // == == == == == == == == == == ==  Set LogOut button Functionality  == == == == == == == == == == == == == ==
    private fun setLogOut() {
        AuthRepository().logout()
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }


    private fun deleteUserAccount() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val firestore = FirebaseFirestore.getInstance()
            val realtimeDb = FirebaseDatabase.getInstance().reference

            // 1. Delete Firestore Data
            firestore.collection("users").document(user.uid).delete()
                .addOnSuccessListener {
                    // 2. Delete Realtime Database Data
                    realtimeDb.child("UsersDB").child(user.uid).removeValue()
                        .addOnSuccessListener {
                            // 3. Delete Active Users Data
                            realtimeDb.child("ActiveUsers").child(user.uid).removeValue()
                                .addOnSuccessListener {
                                    user.delete()
                                        .addOnSuccessListener {
                                            setToast("Account deleted successfully!")
                                            startActivity(Intent(this, AuthActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            setToast("Failed to delete authentication")
                                        }
                                }
                                .addOnFailureListener {
                                    setToast("Failed to delete ActiveUsers data")
                                }
                        }
                        .addOnFailureListener {
                            setToast("Failed to delete UsersDB data")
                        }
                }
                .addOnFailureListener {
                    setToast("Failed to delete Firestore data")
                }
        } else {
            setToast("No user is logged in!")
        }
    }

    override fun onResume() {
        super.onResume()
        setUserStatus("Online")
    }

    override fun onPause() {
        super.onPause()
        setUserStatus("Offline")
    }

    private fun setUserStatus(status: String) {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let {
            val userId = it.uid
            val userRef = firebaseDatabase.reference.child("ActiveUsers").child(userId)

            val userStatus = mapOf(
                "status" to status,
                "uid" to userId,
                "last_seen" to ServerValue.TIMESTAMP
            )

            userRef.updateChildren(userStatus).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseSuccess", "User status updated successfully!")
                } else {
                    Log.e(
                        "FirebaseError",
                        "Failed to update user status: ${task.exception?.message}"
                    )
                }
            }
        }
    }

}