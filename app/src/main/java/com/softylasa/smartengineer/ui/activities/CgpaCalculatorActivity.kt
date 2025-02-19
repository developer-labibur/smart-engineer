package com.softylasa.smartengineer.ui.activities

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.softylasa.smartengineer.databinding.ActivityCgpaCalculatorBinding
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.DialogDepartmentInformationBinding
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity

class CgpaCalculatorActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityCgpaCalculatorBinding
    private val semesterInputs = mutableListOf<TextInputEditText>()
    private val weights2022 = listOf(0.05, 0.05, 0.10, 0.10, 0.20, 0.20, 0.20, 0.10)
    private val weights2010 = listOf(0.05, 0.05, 0.05, 0.15, 0.15, 0.20, 0.25, 0.10)
    private val weights2016 = listOf(0.05, 0.05, 0.05, 0.10, 0.15, 0.20, 0.25, 0.15)
    private var currentWeights = weights2022

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCgpaCalculatorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvResult.visibility = View.GONE

        binding.btnBack.setOnClickListener { finish() }

        setupSemesterInputs()
        setupYearButtons()
        setupActionButtons()
    }

    private fun setupSemesterInputs() {
        semesterInputs.addAll(listOf(
            binding.etSemester1,
            binding.etSemester2,
            binding.etSemester3,
            binding.etSemester4,
            binding.etSemester5,
            binding.etSemester6,
            binding.etSemester7,
            binding.etSemester8
        ))
    }

    private fun setupYearButtons() {
        val yearButtons = listOf(binding.btn2010, binding.btn2016, binding.btn2022)
        yearButtons.forEach { button ->
            button.setOnClickListener {
                yearButtons.forEach { it.setBackgroundResource(R.drawable.header_secondary_bg) }
                button.setBackgroundResource(R.drawable.header_primary_bg)
                binding.tvRegulation.text = getString(R.string.probidhan, button.text)
                updateWeights(button.id)
            }
        }
    }

    private fun updateWeights(selectedButtonId: Int) {
        currentWeights = when(selectedButtonId) {
            R.id.btn2010 -> weights2010
            R.id.btn2016 -> weights2016
            else -> weights2022
        }
    }

    private fun setupActionButtons() {
        binding.btnReset.setOnClickListener { resetAllInputs() }
        binding.btnCalculate.setOnClickListener { calculateCGPA() }
    }

    private fun resetAllInputs() {
        semesterInputs.forEach { it.text?.clear() }
    }

    private fun calculateCGPA() {
        if (semesterInputs.any { it.text.isNullOrEmpty() }) {
            Toast.makeText(this, "Please fill in all semester inputs", Toast.LENGTH_SHORT).show()
            return
        }
        var total = 0.0
        for ((index, input) in semesterInputs.withIndex()) {
            val value = input.text.toString().toDoubleOrNull() ?: 0.0
            if (value > 4.0) {
                input.error = "Max 4.00"
                return
            }
            total += value * currentWeights[index]
        }

        val formattedCGPA = "%.2f".format(total)

        binding.tvResult.apply {
            visibility = View.VISIBLE
            text = formattedCGPA
        }
    }
}