package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivityDepartmentBinding
import com.rudhashi.smartengineer.databinding.DialogDepartmentInformationBinding

class DepartmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDepartmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDepartmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarFontColor()

        // Retrieve data from Intent
        val dAbbr = intent.getStringExtra("DAbbr")
        val dCode = intent.getStringExtra("DCode")
        val dCover = intent.getStringExtra("DCover")
        val dInfo = intent.getStringExtra("DInfo")
        val dName = intent.getStringExtra("DName")

        binding.apply {

            Glide.with(this@DepartmentActivity)
                .load(dCover)
                .into(depCover)
            depName.text = dName
            sortCode.text = getString(R.string.sordCode, dAbbr, dCode)

            btnBack.setOnClickListener{ finish() }
            dep1st.setOnClickListener{ goToAllBookActivity(dAbbr,"1", dName, "1st Semester") }
            dep2nd.setOnClickListener{ goToAllBookActivity(dAbbr, "2", dName, "2nd Semester") }
            dep3rd.setOnClickListener{ goToAllBookActivity(dAbbr, "3", dName, "3rd Semester") }
            dep4th.setOnClickListener{ goToAllBookActivity(dAbbr, "4", dName, "4th Semester") }
            dep5th.setOnClickListener{ goToAllBookActivity(dAbbr, "5", dName, "5th Semester") }
            dep6th.setOnClickListener{ goToAllBookActivity(dAbbr, "6", dName, "6th Semester") }
            dep7th.setOnClickListener{ goToAllBookActivity(dAbbr, "7", dName, "7th Semester") }
            btnInfo.setOnClickListener { getDepartmentInfo(dCover, dName, dInfo) }
        }

    }

    private fun setStatusBarFontColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance( 0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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
    private fun goToAllBookActivity(dAbbr: String?, semNumber: String, depName: String?, semName: String){
        val intent = Intent(this@DepartmentActivity, AllBookActivity::class.java)
        intent.putExtra("fieldName", "xSem")
        intent.putExtra("semesterCode", "${dAbbr}$semNumber")
        intent.putExtra("departmentName", depName)
        intent.putExtra("semesterName", semName)
        startActivity(intent)
    }
}