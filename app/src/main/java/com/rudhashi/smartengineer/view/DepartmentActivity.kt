package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivityDepartmentBinding
import com.rudhashi.smartengineer.databinding.DialogDepartmentInformationBinding
import com.rudhashi.smartengineer.model.CategoryDataClass
import com.rudhashi.smartengineer.old.util.SmartThemeActivity

class DepartmentActivity : SmartThemeActivity() {
    private lateinit var binding: ActivityDepartmentBinding
    private var firestoreDB: FirebaseFirestore? = null

    private lateinit var depCode: String
    private lateinit var dAbbr: String
    private lateinit var dCover: String
    private lateinit var dInfo: String
    private lateinit var dName: String

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDepartmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the status bar background to transparent
        window.statusBarColor = Color.TRANSPARENT

        // Ensure the status bar text color is white
        // Clear only the light status bar flag to force white text, without affecting navigation bar
        val flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

        firestoreDB = FirebaseFirestore.getInstance()
        depCode = intent.getStringExtra("DCode").toString()

        setUpDepartmentAllData(depCode)
        isShimmerOn(true)
    }

    private fun setUpDepartmentAllData(dCode: String) {
        dCode.let { depCode ->
            firestoreDB!!.collection("departments").document(depCode)
                .get().addOnSuccessListener {
                    isShimmerOn(false)

                    val department = it.toObject<CategoryDataClass>()
                    dAbbr = department?.DAbbr ?: ""
                    dCover = department?.DCover ?: ""
                    dInfo = department?.DInfo ?: ""
                    dName = department?.DName ?: ""

                    // Retrieve data from FireStore
                    binding.apply {
                        Glide.with(this@DepartmentActivity).load(dCover).into(depCover)
                        depName.text = dName
                        sortCode.text = getString(R.string.sordCode, dAbbr, dCode)

                        btnBack.setOnClickListener { finish() }
                        dep1st.setOnClickListener { goToAllBookActivity("1", "1st Semester") }
                        Glide.with(this@DepartmentActivity).load(getString(R.string.IconSemester1)).into(ivS1)
                        dep2nd.setOnClickListener { goToAllBookActivity("2", "2nd Semester") }
                        Glide.with(this@DepartmentActivity).load(getString(R.string.IconSemester2)).into(ivS2)
                        dep3rd.setOnClickListener { goToAllBookActivity("3", "3rd Semester") }
                        Glide.with(this@DepartmentActivity).load(getString(R.string.IconSemester3)).into(ivS3)
                        dep4th.setOnClickListener { goToAllBookActivity("4", "4th Semester") }
                        Glide.with(this@DepartmentActivity).load(getString(R.string.IconSemester4)).into(ivS4)
                        dep5th.setOnClickListener { goToAllBookActivity("5", "5th Semester") }
                        Glide.with(this@DepartmentActivity).load(getString(R.string.IconSemester5)).into(ivS5)
                        dep6th.setOnClickListener { goToAllBookActivity("6", "6th Semester") }
                        Glide.with(this@DepartmentActivity).load(getString(R.string.IconSemester6)).into(ivS6)
                        dep7th.setOnClickListener { goToAllBookActivity("7", "7th Semester") }
                        Glide.with(this@DepartmentActivity).load(getString(R.string.IconSemester7)).into(ivS7)
                        btnInfo.setOnClickListener { getDepartmentInfo(dCover, dName, dInfo) }
                    }
                }.addOnFailureListener { isShimmerOn(false) }
        }
    }

    @SuppressLint("InflateParams")
    private fun getDepartmentInfo(dCover: String?, dName: String?, dInfo: String?) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_department_information, null)
        val dialogBinding: DialogDepartmentInformationBinding =
            DialogDepartmentInformationBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.apply {
            Glide.with(this@DepartmentActivity)
                .load(dCover)
                .into(depCover)
            depName.text = dName
            val formattedInfo = dInfo?.replace("\\n", "\n")
            depInfo.text = formattedInfo
            btnClose.setOnClickListener { builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    private fun goToAllBookActivity(semNumber: String, semName: String) {
        val intent = Intent(this@DepartmentActivity, AllBookActivity::class.java)
        intent.putExtra("abbrNo", "${dAbbr}$semNumber")
        intent.putExtra("noAbbr", "${semNumber}$dAbbr")
        intent.putExtra("departmentName", dName)
        intent.putExtra("semesterName", semName)
        startActivity(intent)
    }

    private fun isShimmerOn(flag: Boolean) {
        if (flag) {
            // If data is available then Shimmer Effect will be gone
            binding.shimmerDepartment.startShimmer()
            binding.shimmerDepartment.visibility = View.VISIBLE
            binding.department.visibility = View.GONE
        } else {
            binding.shimmerDepartment.stopShimmer()
            binding.shimmerDepartment.visibility = View.GONE
            binding.department.visibility = View.VISIBLE
        }
    }

}