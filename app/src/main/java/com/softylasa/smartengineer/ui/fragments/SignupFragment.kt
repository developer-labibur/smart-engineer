package com.softylasa.smartengineer.ui.fragments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.utils.helpers.GetSet.getCurrentDate
import com.softylasa.smartengineer.databinding.FragmentSignupBinding
import com.softylasa.smartengineer.databinding.LayoutSidebarBinding
import com.softylasa.smartengineer.data.models.User
import com.softylasa.smartengineer.utils.helpers.AuthRepository
import com.softylasa.smartengineer.utils.helpers.GoogleAuthClient
import com.softylasa.smartengineer.utils.helpers.MakeFirebaseEasy
import com.softylasa.smartengineer.utils.helpers.SmartConfig
import com.softylasa.smartengineer.utils.helpers.SmartData
import com.softylasa.smartengineer.ui.activities.SmartDashboardActivity
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private var tagLog = "SignupFragment"
    private lateinit var sidebarBinding: LayoutSidebarBinding
    private lateinit var layoutInflater: LayoutInflater

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var authRepository: AuthRepository
    private lateinit var googleAuthClient: GoogleAuthClient

    // Dark Mode
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var isNightMode: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        // Initialize Firebase Auth and Firebase Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        authRepository = AuthRepository()
        googleAuthClient = GoogleAuthClient(requireActivity())

        // Called the sidebar functionality
        sideBarLayout()

        // Initialize Firebase Auth and Firebase Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.apply {
            googleCntBtn.setOnClickListener {
                lifecycleScope.launch {

                    val isSignIn = googleAuthClient.signIn(false)
                    if (isSignIn) {
                        val id = auth.currentUser?.uid
                        val email = auth.currentUser?.email
                        try {

                            database.reference.child("Users")
                                .child(id!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val user = snapshot.getValue(User::class.java)
                                        val userName = user?.name?.ifEmpty { auth.currentUser?.displayName }
                                        val role = user?.role.toString()
                                        val isUserNew = user?.joiningDate?.ifEmpty { getCurrentDate() }
                                        val newUserBonusPoints = if (user?.joiningDate!!.isNotEmpty()) user.points else SmartConfig.NEW_USER_SIGNUP_BONUS_POINT
                                        val newUserBonusPointsAll = if (user.joiningDate.isNotEmpty()) user.pointsAll else SmartConfig.NEW_USER_SIGNUP_BONUS_POINT

                                        try{
                                            when (role) {
                                                "Main Admin" -> {
                                                    val isImageNull = user.picture.ifEmpty { auth.currentUser?.photoUrl }

                                                    MakeFirebaseEasy.updateUserInfo(userName, email, isImageNull.toString(), "Main Admin", auth.currentUser?.uid, isUserNew, newUserBonusPoints, newUserBonusPointsAll)

                                                    it.findNavController().navigate(R.id.action_signupFragment_to_smartDashboardActivity)
                                                    SmartData.lToast(requireActivity(), "Success, You are an Main Admin")
                                                }
                                                "Admin" -> {
                                                    val isImageNull = user.picture.ifEmpty { auth.currentUser?.photoUrl }

                                                    MakeFirebaseEasy.updateUserInfo(userName, email, isImageNull.toString(), "Admin", auth.currentUser?.uid, isUserNew, newUserBonusPoints, newUserBonusPointsAll)

                                                    it.findNavController().navigate(R.id.action_signupFragment_to_smartDashboardActivity)
                                                    SmartData.lToast(requireActivity(), "Success, You are an Administrator")
                                                }
                                                else -> {
                                                    val isImageNull = user.picture.ifEmpty { auth.currentUser?.photoUrl }

                                                    MakeFirebaseEasy.updateUserInfo(userName, email, isImageNull.toString(), "User", auth.currentUser?.uid, isUserNew, newUserBonusPoints, newUserBonusPointsAll)

                                                    it.findNavController().navigate(R.id.action_signupFragment_to_smartDashboardActivity)
                                                    SmartData.lToast(requireActivity(), "Login Successfully")
                                                }
                                            }
                                        } catch (e: Exception) {

                                            MakeFirebaseEasy.updateUserInfo(userName, email,"", "User", auth.uid, isUserNew, newUserBonusPoints, newUserBonusPointsAll)

                                            val intent = Intent(requireContext(), SmartDashboardActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            SmartData.lToast(requireActivity(), "SignIn Successfully")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })

                        } catch (e: Exception){
                            Log.d(tag, "Catch exception", e)
                            SmartData.lToast(requireActivity(), "Something went wrong...")
                        }
                    } else {
                        SmartData.lToast(requireActivity(), "SignIn Failed")
                    }

                }
            }

            signupButton.setOnClickListener {
                val user = signupName.text.toString().trim()
                val email = signupEmail.text.toString().trim()
                val pass = signupPassword.text.toString().trim()

                if (user.isEmpty() && email.isEmpty() && pass.isEmpty()) {
                    signupName.error = "Name cannot be empty"
                    signupEmail.error = "Email cannot be empty"
                    signupPassword.error = "Password cannot be empty"
                } else {

                    try {
                        Log.w(tagLog, "Enter Signup Fragment - Try Block")
                        lifecycleScope.launch {
                            val isSignupSuccess = authRepository.register(user, email, pass)

                            if (isSignupSuccess) {
                                SmartData.lToast(requireActivity(), "SignUp Successful")
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.navHostFragmentContainerView, LoginFragment())
                                    .commit()
                                Log.w(tagLog, "isSignupSuccess")
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        SmartData.lToast(
                            requireActivity(),
                            "The email address is already in use by another account."
                        )
                        // TODO: App ai khane ashe crush kore bar bar - Gmail Already Use kora hole
                        Log.w(tagLog, "Enter Signup Fragment - Catch Block", e)
                    }
                }
            }

            btnLogin.setOnClickListener {
                try {
                    Log.d(tagLog, "Fragment replaced via NavHostController")
                    it.findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
                } catch (e: Exception) {
                    Log.d(tagLog, "Fragment replaced via FragmentTransaction")
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.navHostFragmentContainerView, LoginFragment()).commit()
                }
            }
        }

        return binding.root
    }

    // ----------------------------------------------------------------
    private fun sideBarLayout() {
        // Inflate the sidebar layout
        layoutInflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Initialize the sidebar layout binding
        sidebarBinding = LayoutSidebarBinding.inflate(layoutInflater)

        // Add the sidebar layout to the ViewGroup
        binding.sideBar.addView(sidebarBinding.root)

        // Initialize the night mode toggle
        nightModeToggle()

        // Set click listeners for the sidebar buttons
        sidebarBinding.apply {
            // Set Selected Button and deactivate buttons styles
            sideBarBtnSignUp.setBackgroundResource(R.drawable.sl_button_selected)
            sideBarBtnLogin.setBackgroundResource(R.color.sideBar_Bg)
            //sideBarBtnGoogle.setBackgroundResource(R.color.sideBar_Bg)
            //sideBarBtnFacebook.setBackgroundResource(R.color.sideBar_Bg)
            sideBarTitle.text = resources.getString(R.string.signup)

            // Set click listeners for the sidebar buttons
            sideBarBtnLogin.setOnClickListener { fragmentSwitch(LoginFragment()) }
            sideBarBtnSignUp.setOnClickListener { fragmentSwitch(SignupFragment()) }
            sideBarBtnGoogle.setOnClickListener {
                lifecycleScope.launch {

                    val isSignIn = googleAuthClient.signIn(false)
                    if (isSignIn) {
                        val id = auth.currentUser?.uid
                        val email = auth.currentUser?.email
                        try {

                            database.reference.child("Users")
                                .child(id!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val user = snapshot.getValue(User::class.java)
                                        val userName = user?.name?.ifEmpty { auth.currentUser?.displayName }
                                        val role = user?.role.toString()
                                        val isUserNew = user?.joiningDate?.ifEmpty { getCurrentDate() }
                                        val newUserBonusPoints = if (user?.joiningDate!!.isNotEmpty()) user.points else SmartConfig.NEW_USER_SIGNUP_BONUS_POINT
                                        val newUserBonusPointsAll = if (user.joiningDate.isNotEmpty()) user.pointsAll else SmartConfig.NEW_USER_SIGNUP_BONUS_POINT

                                        try{
                                            when (role) {
                                                "Main Admin" -> {
                                                    val isImageNull = user.picture.ifEmpty { auth.currentUser?.photoUrl }

                                                    MakeFirebaseEasy.updateUserInfo(userName, email, isImageNull.toString(), "Main Admin", auth.currentUser?.uid, isUserNew, newUserBonusPoints, newUserBonusPointsAll)

                                                    it.findNavController().navigate(R.id.action_signupFragment_to_smartDashboardActivity)
                                                    SmartData.lToast(requireActivity(), "Success, You are an Main Admin")
                                                }
                                                "Admin" -> {
                                                    val isImageNull = user.picture.ifEmpty { auth.currentUser?.photoUrl }

                                                    MakeFirebaseEasy.updateUserInfo(userName, email, isImageNull.toString(), "Admin", auth.currentUser?.uid, isUserNew, newUserBonusPoints, newUserBonusPointsAll)

                                                    it.findNavController().navigate(R.id.action_signupFragment_to_smartDashboardActivity)
                                                    SmartData.lToast(requireActivity(), "Success, You are an Administrator")
                                                }
                                                else -> {
                                                    val isImageNull = user.picture.ifEmpty { auth.currentUser?.photoUrl }

                                                    MakeFirebaseEasy.updateUserInfo(userName, email, isImageNull.toString(), "User", auth.currentUser?.uid, isUserNew, newUserBonusPoints, newUserBonusPointsAll)

                                                    it.findNavController().navigate(R.id.action_signupFragment_to_smartDashboardActivity)
                                                    SmartData.lToast(requireActivity(), "Login Successfully")
                                                }
                                            }
                                        } catch (e: Exception) {

                                            MakeFirebaseEasy.updateUserInfo(userName, email,"", "User", auth.uid, isUserNew, newUserBonusPoints, newUserBonusPointsAll)

                                            val intent = Intent(requireContext(), SmartDashboardActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            SmartData.lToast(requireActivity(), "SignIn Successfully")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })

                        } catch (e: Exception){
                            Log.d(tag, "Catch exception", e)
                            SmartData.lToast(requireActivity(), "Something went wrong...")
                        }
                    } else {
                        SmartData.lToast(requireActivity(), "SignIn Failed")
                    }

                }
            }
            sideBarBtnFacebook.setOnClickListener {
                lifecycleScope.launch {
                    googleAuthClient.signOut()
                    Toast.makeText(context, "SignOut", Toast.LENGTH_SHORT).show()
                }
            }
            sideBarBtnSettings.setOnClickListener {
                if (isNightMode as Boolean) {
                    editor.putBoolean(SmartData.SWITCH_BUTTON_KEY, true).apply()
                    sideBarBtnSettings.setImageResource(R.drawable.sl_moon)
                    sideBarBtnSettings.setBackgroundResource(R.drawable.night_mode_thumb_true)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    isNightMode = false
                } else {
                    editor.putBoolean(SmartData.SWITCH_BUTTON_KEY, false).apply()
                    sideBarBtnSettings.setImageResource(R.drawable.sl_sun)
                    sideBarBtnSettings.setBackgroundResource(R.drawable.night_mode_thumb_false)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    isNightMode = true
                }
            }
        }
    }

    private fun nightModeToggle() {
        // Initialize the binding
        sharedPreferences = context?.getSharedPreferences(SmartData.PREF_KEY, MODE_PRIVATE)!!
        editor = sharedPreferences.edit()

        // Get the current theme mode
        isNightMode = sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)
        if (isNightMode as Boolean) {
            sidebarBinding.sideBarBtnSettings.setImageResource(R.drawable.sl_moon)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            sidebarBinding.sideBarBtnSettings.setImageResource(R.drawable.sl_sun)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun fragmentSwitch(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragmentContainerView, fragment).commit()
    }
}
