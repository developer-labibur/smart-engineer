package com.rudhashi.smartengineer.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.data.constants.Converters.dpToPx
import com.rudhashi.smartengineer.data.constants.GetSet.lToast
import com.rudhashi.smartengineer.databinding.ActivityProfileBinding
import com.rudhashi.smartengineer.databinding.DialogEditProfileBinding
import com.rudhashi.smartengineer.data.models.User
import com.rudhashi.smartengineer.data.repositories.AuthRepository
import com.rudhashi.smartengineer.manager.CUserManager
import com.rudhashi.smartengineer.managers.DialogManager.showNegativeInfoDialog
import com.rudhashi.smartengineer.old.util.MakeFirebaseEasy
import com.rudhashi.smartengineer.old.util.MakeFirebaseEasy.Companion.uploadProfileImage
import com.rudhashi.smartengineer.old.util.SmartData


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var dialogBinding: DialogEditProfileBinding
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

        try {
            Log.e(tagLog, "Try: All okay")
            //loadProfilePicture()
            //loadProfileInfo()
        } catch (e: Exception) {
            Log.e(tagLog, "Catch: Is called")
        }

        binding.apply {
            backProfileBtn.setOnClickListener { finish() }
            proBtnEdit.setOnClickListener { setProfileInfo() }
            backLogout.setOnClickListener {
                showNegativeInfoDialog(
                    this@ProfileActivity,
                    R.raw.anim_logout,
                    "Logout",
                    "Are you sure you want to logout?"
                ) { setLogOut() }
            }
            proBtnDelete.setOnClickListener {
                showNegativeInfoDialog(
                    this@ProfileActivity,
                    R.raw.anim_delete,
                    "Delete Account",
                    "Are you sure you want to delete your account? This action cannot be undone."
                ) { deleteUserAccount() }
            }

            btnSettings.setOnClickListener {
                val intent = Intent(this@ProfileActivity, SmartDashboardActivity::class.java)
                intent.putExtra("DISPLAY", "Setting")
                startActivity(intent)
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
    }

    // == == == == == == == == == == ==  Edit Profile button Functionality  == == == == == == == == == == == == == ==
    @SuppressLint("InflateParams")
    private fun setProfileInfo() {
        val builder = AlertDialog.Builder(this@ProfileActivity).create()
        val dialogView =
            LayoutInflater.from(this@ProfileActivity).inflate(R.layout.dialog_edit_profile, null)
        dialogBinding = DialogEditProfileBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        val semesterList = resources.getStringArray(R.array.semester)
        val adapterDepartment: ArrayAdapter<String> = ArrayAdapter<String>(
            this@ProfileActivity,
            R.layout.item_dropdown,
            MakeFirebaseEasy.getAllDepartmentName(ArrayList())
        )

        dialogBinding.apply {
            // Fetch institute names and populate the AutoCompleteTextView
            fetchInstituteNames(editProInstitute)

            // Set up department and semester adapters
            editProDepartment.setAdapter(adapterDepartment)
            val adapterSemester: ArrayAdapter<String> =
                ArrayAdapter<String>(this@ProfileActivity, R.layout.item_dropdown, semesterList)
            editProSemester.setAdapter(adapterSemester)

            // Fetch and display user data
            val userDocRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)

            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    Glide.with(this@ProfileActivity)
                        .load(user?.picture?.let { MakeFirebaseEasy.currentUserImage(it) })
                        .placeholder(R.drawable.avatar)
                        .into(proPic)
                    editProName.setText(user?.name)
                    editProInstitute.setText(user?.institute) // Pre-fill institute
                }
            }.addOnFailureListener {
                Toast.makeText(this@ProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }

            dialogBinding.btnSave.setOnClickListener {
                val userName = editProName.text.toString()
                val userDepartment = editProDepartment.text.toString()
                val userSemester = editProSemester.text.toString()
                val userInstitute = editProInstitute.text.toString()

                if (this@ProfileActivity::selectedImage.isInitialized) {
                    uploadProfileImage(selectedImage, currentUser.uid, { imageUrl ->
                        val updatedUserInfo = mapOf(
                            "name" to userName,
                            "department" to userDepartment,
                            "semester" to userSemester,
                            "institute" to userInstitute,
                            "picture" to imageUrl
                        )
                        Firebase.database.reference.child("Users").child(auth.uid!!)
                            .updateChildren(updatedUserInfo)
                            .addOnSuccessListener {
                                Toast.makeText(this@ProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                builder.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@ProfileActivity, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }, useFirebaseStorage = false) // Set true for Firebase Storage, false for Retrofit
                } else {
                    val updatedUserInfo = mutableMapOf(
                        "name" to userName,
                        "department" to userDepartment,
                        "semester" to userSemester,
                        "institute" to userInstitute,
                    )
                    userDocRef.get().addOnSuccessListener { documentSnapshot ->
                        val user = documentSnapshot.toObject(User::class.java)
                        updatedUserInfo["picture"] = user?.picture ?: ""

                        userDocRef.update(updatedUserInfo as Map<String, Any>)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Profile updated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                builder.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Error updating profile: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }.addOnFailureListener {
                        Toast.makeText(this@ProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            dialogBinding.btnCancel.setOnClickListener { builder.dismiss() }
            dialogBinding.editPro.setOnClickListener { callImagePicker() }
            dialogBinding.proCamera.setOnClickListener { callImagePicker() }
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
                        dialogBinding.proPic.setImageURI(it)
                        selectedImage = it
                        SmartData.sToast(this@ProfileActivity, "Image Selected")
                    }
                }

                ImagePicker.RESULT_ERROR -> SmartData.sToast(
                    this,
                    ImagePicker.getError(result.data)
                )

                else -> SmartData.sToast(this, "Not Select Any Image")
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

        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // User account deleted from Firebase Auth
                lToast(this, "Account deleted successfully!")

                // Optional: Navigate to a different screen
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            } else {
                // Handle deletion failure
                val errorMessage = task.exception?.localizedMessage ?: "Failed to delete account."
                lToast(this, errorMessage)
            }
        }
    }


    inner class InstituteAdapter(private val institutes: List<String>) :
        ArrayAdapter<String>(this, R.layout.item_dropdown, institutes) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val instituteName = getItem(position)
            val instituteNameText: TextView = view.findViewById(R.id.tv)
            instituteNameText.text = instituteName // Set the institute name
            return view
        }
    }

    private fun fetchInstituteNames(editProInstitute: MaterialAutoCompleteTextView) {
        firestore.collection("institutes")
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                val instituteNames = mutableListOf<String>()
                for (document in result) {
                    val instituteName = document.getString("name")
                    if (instituteName != null) {
                        instituteNames.add(instituteName)
                    }
                }

                // Create an instance of the custom adapter
                val adapter = InstituteAdapter(instituteNames)

                // Set the custom adapter to the AutoCompleteTextView
                editProInstitute.setAdapter(adapter)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace() // Handle error
            }
    }

}