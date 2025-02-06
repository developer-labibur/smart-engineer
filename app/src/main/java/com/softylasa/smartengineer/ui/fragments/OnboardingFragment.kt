package com.softylasa.smartengineer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingBinding

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = arguments?.getInt(ARG_POSITION) ?: 0
        setupOnboardingContent(position)
    }

    private fun setupOnboardingContent(position: Int) {
        when (position) {
            0 -> {
                binding.textTitle.text = getString(R.string.onboarding_title_1)
                binding.textDescription.text = getString(R.string.onboarding_description_1)
                binding.image.setImageResource(R.drawable.onboarding_1_welcome)
            }
            1 -> {
                binding.textTitle.text = getString(R.string.onboarding_title_2)
                binding.textDescription.text = getString(R.string.onboarding_description_2)
                binding.image.setImageResource(R.drawable.onboarding_2_access_study_materials)
            }
            2 -> {
                binding.textTitle.text = getString(R.string.onboarding_title_3)
                binding.textDescription.text = getString(R.string.onboarding_description_3)
                binding.image.setImageResource(R.drawable.onboarding_3_stay_informed)
            }
            3 -> {
                binding.textTitle.text = getString(R.string.onboarding_title_4)
                binding.textDescription.text = getString(R.string.onboarding_description_4)
                binding.image.setImageResource(R.drawable.onboarding_4_explore_and_connect)
            }
        }
    }
}