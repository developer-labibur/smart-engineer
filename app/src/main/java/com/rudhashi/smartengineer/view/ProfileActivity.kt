package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivityProfileBinding
import com.rudhashi.smartengineer.databinding.DialogEditProfileBinding
import com.rudhashi.smartengineer.databinding.DialogLogoutWarningBinding
import com.rudhashi.smartengineer.model.User
import com.rudhashi.smartengineer.util.MakeFirebaseEasy
import com.rudhashi.smartengineer.util.SmartData


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var dialogBinding: DialogEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var fbDB: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var selectedImage: Uri
    private val tagLog = "ProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        fbDB = Firebase.database.reference
        currentUser = auth.currentUser!!

        try {
            Log.e(tagLog, "Try: All okay")
            loadProfilePicture()
            loadProfileInfo()
        } catch (e: Exception) {
            Log.e(tagLog, "Catch: Is called")
        }

        binding.apply {
            backProfileBtn.setOnClickListener { finish() }
            proBtnEdit.setOnClickListener { setProfileInfo() }
            backLogout.setOnClickListener { setLogOut() }
            proBtnDelete.setOnClickListener { SmartData.comingSoon(this@ProfileActivity) }

            btnSettings.setOnClickListener {
                val intent = Intent(this@ProfileActivity, SmartDashboardActivity::class.java)
                intent.putExtra("DISPLAY", "Setting")
                startActivity(intent)
            }
        }
    }

    // == == == == == == == == == == ==  Load Profile Information and Picture  == == == == == == == == == == == == == ==
    private fun loadProfileInfo() {
        database.reference.child("Users").child(auth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: User? = snapshot.getValue(User::class.java)
                    binding.proName.text = user?.name
                    binding.proEmail.text = user?.email
                    binding.proDepartment.text = user?.department
                    binding.proInstitute.text = user?.institute
                    binding.proSemester.text = user?.semester
                }

                override fun onCancelled(error: DatabaseError) {
                    SmartData.sToast(this@ProfileActivity, "Someting went wrong...")
                }
            })
    }
    private fun loadProfilePicture() {
        database.reference.child("Users").child(auth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: User? = snapshot.getValue(User::class.java)

                    Glide.with(this@ProfileActivity)
                        .load(user?.picture?.let { MakeFirebaseEasy.currentUserImage(it) })
                        .placeholder(R.drawable.avatar)
                        .into(binding.proPic)

                }

                override fun onCancelled(error: DatabaseError) {
                    SmartData.sToast(this@ProfileActivity, "Someting went wrong...")
                }
            })
    }

    // == == == == == == == == == == ==  Edit Profile button Functionality  == == == == == == == == == == == == == ==
    @SuppressLint("InflateParams")
    private fun setProfileInfo() {
        val builder = AlertDialog.Builder(this@ProfileActivity, R.style.CustomAlertDialog).create()
        val dialogView = LayoutInflater.from(this@ProfileActivity).inflate(R.layout.dialog_edit_profile, null)
        dialogBinding = DialogEditProfileBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        val semesterList = resources.getStringArray(R.array.semester)
        val adapterDepartment: ArrayAdapter<String> = ArrayAdapter<String>(
            this@ProfileActivity, R.layout.item_dropdown, MakeFirebaseEasy.getAllDepartmentName(ArrayList()))

        dialogBinding.apply {
            editProDepartment.setAdapter(adapterDepartment)
            val adapterSemester: ArrayAdapter<String> = ArrayAdapter<String>(this@ProfileActivity, R.layout.item_dropdown, semesterList)
            editProSemester.setAdapter(adapterSemester)

            database.reference.child("Users")
                .child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        Glide.with(this@ProfileActivity)
                            .load(user?.picture?.let { MakeFirebaseEasy.currentUserImage(it) })
                            .placeholder(R.drawable.avatar)
                            .into(proPic)
                        editProName.setText(user?.name)
                        editProInstitute.setText(user?.institute)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

            dialogBinding.btnSave.setOnClickListener {
                val userName = editProName.text.toString()
                val userDepartment = editProDepartment.text.toString()
                val userSemester = editProSemester.text.toString()
                val userInstitute = editProInstitute.text.toString()

                if (this@ProfileActivity::selectedImage.isInitialized){
                    MakeFirebaseEasy.uploadProfileImage(selectedImage, currentUser.uid)
                    val updatedUserInfo = hashMapOf("name" to userName, "department" to userDepartment, "semester" to userSemester, "institute" to userInstitute)
                    database.reference.child("Users").child(auth.uid!!).updateChildren(updatedUserInfo as Map<String, Any>)
                    loadProfileInfo()
                    builder.dismiss()
                } else {
                    database.reference.child("Users")
                        .child(currentUser.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue(User::class.java)
                                val updatedUserInfo = hashMapOf("name" to userName, "department" to userDepartment, "semester" to userSemester, "institute" to userInstitute, "picture" to user?.picture.toString())
                                database.reference.child("Users").child(auth.uid!!).updateChildren(updatedUserInfo as Map<String, Any>)
                                loadProfileInfo()
                                loadProfilePicture()
                                builder.dismiss()
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }
            dialogBinding.btnCancel.setOnClickListener{ builder.dismiss() }
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
    private val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
            ImagePicker.RESULT_ERROR -> SmartData.sToast(this, ImagePicker.getError(result.data))
            else -> SmartData.sToast(this, "Not Select Any Image")
        }
    }

    // == == == == == == == == == == ==  Set LogOut button Functionality  == == == == == == == == == == == == == ==
    @SuppressLint("InflateParams")
    private fun setLogOut(){
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout_warning, null)
        val dialogBinding = DialogLogoutWarningBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.btnYesLogOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            builder.dismiss()
            finish()
        }
        dialogBinding.btnNoLogOut.setOnClickListener { builder.dismiss() }
        dialogBinding.btnCloseLogOut.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }
}