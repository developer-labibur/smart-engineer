package com.rudhashi.smartengineer.container

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.FragmentNightModeBinding
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.ThemeHelper
import com.rudhashi.smartengineer.util.ThemeHelper.getSharedPreferences
import com.rudhashi.smartengineer.view.SmartDashboardActivity

class NightModeFragment : Fragment() {

    private var _binding: FragmentNightModeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var isNightMode: Boolean? = null
    private val tag = "DayNightFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNightModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the binding
        sharedPreferences = getSharedPreferences(requireActivity())
        editor = sharedPreferences.edit()

        // Get the current theme mode

        binding.apply {
            isNightMode = sharedPreferences.getBoolean(SmartData.SWITCH_BUTTON_KEY, false)

            if (isNightMode as Boolean) {
                animationView.setAnimation(R.raw.moon)
                btnDark.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvDark.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                btnLight.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvLight.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                animationView.setAnimation(R.raw.sun)
                btnDark.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvDark.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                btnLight.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvLight.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            backToHome.setOnClickListener {
                startActivity(Intent(requireActivity(), SmartDashboardActivity::class.java))
                requireActivity().finish()
            }
        }

        // Set up button listeners
        setupListeners()
    }
    // =====================================================================================================================
    // ===========================================     Dark Mode Functionality      ========================================
    // =====================================================================================================================
    private fun setupListeners() {
        binding.apply {
            btnDark.setOnClickListener {
                if (!ThemeHelper.isDarkMode(requireContext())) {
                    changeTheme(true)
                    animationView.setAnimation(R.raw.moon)
                    animationView.playAnimation()
                }
            }

            btnLight.setOnClickListener {
                if (ThemeHelper.isDarkMode(requireContext())) {
                    changeTheme(false)
                    animationView.setAnimation(R.raw.sun)
                    animationView.playAnimation()
                }
            }
        }
    }
    private fun changeTheme(toDarkMode: Boolean) {
        try {
            ThemeHelper.saveThemePreference(requireContext(), toDarkMode)
            ThemeHelper.applyTheme(toDarkMode)
            Log.d(tag, "Theme changed to ${if (toDarkMode) "Dark" else "Light"}.")

            // Safely recreate the parent activity
            activity?.let { parentActivity ->
                if (!parentActivity.isFinishing && !parentActivity.isDestroyed) {
                    parentActivity.recreate()
                } else {
                    Log.w(tag, "Parent activity is not in a valid state for recreation.")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error changing theme: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}