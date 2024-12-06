package com.rudhashi.smartengineer.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.DialogChangeDepartmentBinding
import com.rudhashi.smartengineer.databinding.DialogLogoutWarningBinding
import com.rudhashi.smartengineer.databinding.FragmentSmartSettingBinding
import com.rudhashi.smartengineer.model.User
import com.rudhashi.smartengineer.util.GoogleAuthClient
import com.rudhashi.smartengineer.util.MakeFirebaseEasy
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.view.MainActivity
import com.rudhashi.smartengineer.view.ProfileActivity
import com.rudhashi.smartengineer.view.SmartDashboardActivity
import com.rudhashi.smartengineer.view.SmartEBookActivity
import kotlinx.coroutines.launch

class SmartSettingFragment : Fragment() {

    private lateinit var binding: FragmentSmartSettingBinding

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var dialogDepBinding: DialogChangeDepartmentBinding
    private val userId = auth.currentUser?.uid
    private var database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("Users/$userId")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmartSettingBinding.inflate(inflater, container, false)

        displayUserProfileInfo()  // Display user profile info

        binding.apply {
            btnViewProfile.setOnClickListener { startActivity(Intent(requireActivity(), ProfileActivity::class.java)) }
            btnViewPremium.setOnClickListener { SmartData.comingSoon(requireActivity()) }

            // Custom Section --------------------------------
            sBtnPremium.setOnClickListener { startActivity(Intent(requireActivity(), SmartEBookActivity::class.java)) }
            nightModeToggle()  // Set Dark Mode functionality

            // Special Section --------------------------------
            sBtnInstitute.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            sBtnDepartment.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            sBtnRecycling.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            sBtnCommunity.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            sBtnCGPA.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            sBtnContributor.setOnClickListener { SmartData.comingSoon(requireActivity()) }

            // Management Section --------------------------------
            sBtnProfile.setOnClickListener { startActivity(Intent(requireActivity(), ProfileActivity::class.java)) }
            sBtnChangeDepartment.setOnClickListener { setDepartmentInfo() }
            sBtnLogout.setOnClickListener { setLogOut() }

            // About Us Section --------------------------------
            sBtnDeveloper.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            sBtnRate.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            sBtnPrivacyPolicy.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            sBtnFAQ.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            sBtnAbout.setOnClickListener { SmartData.comingSoon(requireActivity()) }
        }

        return binding.root
    }

    private fun nightModeToggle() {
        val sharedPreferences = requireActivity().getSharedPreferences(SmartData.PREF_KEY, MODE_PRIVATE)

        // Set the initial state of the switch based on saved preference
        val isDarkModeOn = sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)
        binding.switchTheme.isChecked = isDarkModeOn

        // Apply the saved theme
        applyTheme(isDarkModeOn)

        // Set up the switch listener
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            try {
                Log.i("TEST_SL", "nightModeToggle")
                // Save the theme preference
                val editor = sharedPreferences.edit()
                editor.putBoolean(SmartData.SWITCH_BUTTON_KEY, isChecked).apply()
                // Apply the theme
                applyTheme(isChecked)

                val intent = Intent(requireActivity(), SmartDashboardActivity::class.java)
                intent.putExtra("DISPLAY", "Home")
                startActivity(intent)
            } catch (e: Exception){
                Log.i("TEST_SL", "Failed")
            }
        }
    }

    private fun applyTheme(isDarkModeOn: Boolean) {
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun displayUserProfileInfo() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convert snapshot to User object
                val user = snapshot.getValue(User::class.java)

                try {
                    Glide.with(requireActivity())
                        .load(user?.picture?.let { MakeFirebaseEasy.currentUserImage(it) })
                        .transform(RoundedCorners(SmartData.dpToPx(requireActivity(),10f)))
                        .into(binding.ivProfilePicture)
                } catch (e: Exception){
                    Log.i("TEST_SL", "Failed")
                }


                // Update the TextView with the new points
                binding.tvProfileName.text = user?.name
                binding.tvProfileEmail.text = user?.email
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireActivity(), "Failed to load user information", Toast.LENGTH_SHORT).show()
            }
        })
    }
    @SuppressLint("InflateParams")
    private fun setLogOut(){
        val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).create()
        val dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_logout_warning, null)
        val dialogBinding: DialogLogoutWarningBinding =
            DialogLogoutWarningBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.btnYesLogOut.setOnClickListener {
            lifecycleScope.launch {
                GoogleAuthClient(requireActivity()).signOut()
                Toast.makeText(requireActivity(), "SignOut", Toast.LENGTH_SHORT).show()
            }
            //auth.signOut()
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            builder.dismiss()
        }
        dialogBinding.btnNoLogOut.setOnClickListener { builder.dismiss() }
        dialogBinding.btnCloseLogOut.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }





    @SuppressLint("InflateParams")
    private fun setDepartmentInfo() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).create()
        val dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_change_department, null)
        dialogDepBinding = DialogChangeDepartmentBinding.bind(dialogView)
        builder.setView(dialogDepBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        val adapterDepartment: ArrayAdapter<String> = ArrayAdapter<String>(requireActivity(), R.layout.item_dropdown, MakeFirebaseEasy.getAllDepartmentName(ArrayList()))

        dialogDepBinding.apply {
            editProDepartment.setAdapter(adapterDepartment)

            userId?.let {
                database.reference.child("Users")
                    .child(it)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(User::class.java)
                            tvCurrentDep.text =
                                getString(R.string.current_department_is, user?.department)
                        }override fun onCancelled(error: DatabaseError) {}
                    })
            }

            dialogDepBinding.btnSave.setOnClickListener {
                val updatedUserInfo = hashMapOf("department" to editProDepartment.text.toString())
                database.reference.child("Users").child(auth.uid!!).updateChildren(updatedUserInfo as Map<String, Any>)
                builder.dismiss()
            }
            dialogDepBinding.btnCancel.setOnClickListener{ builder.dismiss() }
        }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }
}