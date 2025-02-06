package com.softylasa.smartengineer.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.constants.Constants.APP_LINK
import com.softylasa.smartengineer.data.constants.Constants.PACKAGE_NAME
import com.softylasa.smartengineer.utils.helpers.Converters.dpToPx
import com.softylasa.smartengineer.databinding.FragmentSmartSettingBinding
import com.softylasa.smartengineer.data.models.User
import com.softylasa.smartengineer.data.repositories.AuthRepository
import com.softylasa.smartengineer.managers.CUserManager
import com.softylasa.smartengineer.managers.DialogManager.showFreemiumFeaturesDialog
import com.softylasa.smartengineer.managers.DialogManager.showNegativeInfoDialog
import com.softylasa.smartengineer.ui.activities.AboutUsActivity
import com.softylasa.smartengineer.ui.activities.AcademicBookActivity
import com.softylasa.smartengineer.ui.activities.AllDepartmentActivity
import com.softylasa.smartengineer.ui.activities.AuthActivity
import com.softylasa.smartengineer.utils.helpers.MakeFirebaseEasy
import com.softylasa.smartengineer.utils.helpers.SmartData
import com.softylasa.smartengineer.ui.activities.DayNightActivity
import com.softylasa.smartengineer.ui.activities.NoticeBoardActivity
import com.softylasa.smartengineer.ui.activities.ProfileActivity
import com.softylasa.smartengineer.ui.activities.UpdateProfileActivity
import com.softylasa.smartengineer.ui.activities.WebBrowserActivity

class SmartSettingFragment : Fragment() {

    private lateinit var binding: FragmentSmartSettingBinding

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private var database = FirebaseDatabase.getInstance()

    private val pictureListener: (String) -> Unit = { imageUrl ->
        if (imageUrl.isEmpty()) {
            binding.ivProfilePicture.setImageResource(R.drawable.avatar)
        } else {
            if (isAdded) { // Ensure Fragment is attached
                Glide.with(this)
                    .load(MakeFirebaseEasy.currentUserImage(imageUrl))
                    .placeholder(R.drawable.avatar)
                    .transform(RoundedCorners(dpToPx(requireActivity(), 10f))) // Use requireContext()
                    .into(binding.ivProfilePicture)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmartSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayUserProfileInfo()  // Display user profile info

        binding.apply {
            activity?.let { context ->
                btnViewProfile.setOnClickListener { startActivity(Intent(context, ProfileActivity::class.java)) }
                btnViewPremium.setOnClickListener { showFreemiumFeaturesDialog(context) }

                // Custom Section --------------------------------
                sBtnPremium.setOnClickListener { showFreemiumFeaturesDialog(context) }
                sBtnDark.setOnClickListener {
                    startActivity(Intent(context, DayNightActivity::class.java))
                    context.finish()
                }

                // Special Section --------------------------------
                //sBtnInstitute.setOnClickListener { startActivity(Intent(context, NoticeBoardActivity::class.java)) }
                sBtnDepartment.setOnClickListener { startActivity(Intent(context, AllDepartmentActivity::class.java)) }
                sBtnBtebNotice.setOnClickListener { startActivity(Intent(context, NoticeBoardActivity::class.java)) }
                sBtnAcademicBook.setOnClickListener { startActivity(Intent(context, AcademicBookActivity::class.java)) }
                //sBtnRecycling.setOnClickListener { SmartData.comingSoon(context) }
                //sBtnCommunity.setOnClickListener { SmartData.comingSoon(context) }
                sBtnCGPA.setOnClickListener { SmartData.comingSoon(context) }
                //sBtnContributor.setOnClickListener { SmartData.comingSoon(context) }

                // Management Section --------------------------------
                sBtnProfile.setOnClickListener { startActivity(Intent(context, ProfileActivity::class.java)) }
                sBtnChangeDepartment.setOnClickListener { startActivity(Intent(context, UpdateProfileActivity::class.java)) }
                sBtnLogout.setOnClickListener {
                    showNegativeInfoDialog(
                        context,
                        R.raw.anim_logout,
                        "Logout",
                        "Are you sure you want to logout?"
                    ) { setLogOut() } }

                // About Us Section --------------------------------
                //sBtnDeveloper.setOnClickListener { SmartData.comingSoon(context) }
                sBtnRate.setOnClickListener { rateApp() }
                sBtnShare.setOnClickListener { shareApp() }
                sBtnPrivacyPolicy.setOnClickListener {
                    // Open Privacy Policy
                    openWebView("Privacy Policy", "https://sites.google.com/view/smartengineer-privacy-policy")
                }
                sBtnTerms.setOnClickListener {
                    // Open Terms of Service
                    openWebView("Terms of Service", "https://sites.google.com/view/techlib-terms-and-conditions")
                }
                //sBtnFAQ.setOnClickListener { SmartData.comingSoon(context) }
                sBtnAbout.setOnClickListener { startActivity(Intent(context, AboutUsActivity::class.java)) }
            }

        }
    }

    // =====================================================================================================================
    // ===========================================    Display User Information      ========================================
    // =====================================================================================================================


    private fun displayUserProfileInfo() {
        CUserManager.initialize()
        CUserManager.apply {
            name { binding.tvProfileName.text = it }
            email { binding.tvProfileEmail.text = it }
            picture(pictureListener)
        }
    }

    // == == == == == == == == == == ==  Set LogOut button Functionality  == == == == == == == == == == == == == ==
    private fun setLogOut() {
        AuthRepository().logout()
        activity?.let { context ->
            startActivity(Intent(context, AuthActivity::class.java))
            context.finish()
        }
    }

    private fun openWebView(title: String, url: String) {
        activity?.let { context ->
            val intent = Intent(context, WebBrowserActivity::class.java)
            intent.putExtra("TITLE", title)
            intent.putExtra("URL", url)
            startActivity(intent)
        }
    }


    // == == == == == == == == == == ==  Set Rate App button Functionality  == == == == == == == == == == == == == ==
    private fun rateApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PACKAGE_NAME"))
            intent.setPackage("com.android.vending") // Open Play Store app
            startActivity(intent)
        } catch (e: Exception) {
            // If Play Store app is not installed, open in browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(APP_LINK))
            startActivity(intent)
        }
    }

    // == == == == == == == == == == ==  Set Share App button Functionality  == == == == == == == == == == == == == ==
    private fun shareApp() {
        val shareText = "Check out Smart Engineer App for Polytechnic Students! Download it here: $APP_LINK"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share Smart Engineer App Via"))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the picture listener to avoid callbacks after the activity is destroyed
        CUserManager.removePictureListener(pictureListener)
        CUserManager.cleanup()
    }

}