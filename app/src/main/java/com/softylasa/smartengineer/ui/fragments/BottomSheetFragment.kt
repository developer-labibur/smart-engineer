package com.softylasa.smartengineer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.softylasa.smartengineer.managers.AnimationManager
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.BottomSheetAnimationModeBinding

class AnimationModeBottomSheet : BottomSheetDialogFragment() {
    private lateinit var onAnimationModeSelected: (Int) -> Unit

    // This function is to pass the callback to the fragment to save the selected mode
    fun setOnAnimationModeSelectedListener(listener: (Int) -> Unit) {
        onAnimationModeSelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = BottomSheetAnimationModeBinding.inflate(inflater, container, false)
        val currentMode = AnimationManager.getAnimationMode(requireContext())
        val radioGroup = binding.radioGroup
        val radioButtons = arrayOf(
            binding.animNo,
            binding.animSlide,
            binding.animFlipOver,
            binding.animFlipRight,
            binding.animCubeRotation,
            binding.animShrinkExpand,
            binding.animCarousel
        )

        // Pre-select the current animation mode
        radioButtons[currentMode].isChecked = true

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedMode = when (checkedId) {
                R.id.animNo -> 0
                R.id.animSlide -> 1
                R.id.animFlipOver -> 2
                R.id.animFlipRight -> 3
                R.id.animCubeRotation -> 4
                R.id.animShrinkExpand -> 5
                R.id.animCarousel -> 6
                else -> 0
            }
            onAnimationModeSelected(selectedMode)
        }

        return binding.root
    }
}