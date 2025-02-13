package com.softylasa.smartengineer.ui.activities

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.softylasa.smartengineer.databinding.ActivityCgpaCalculatorBinding
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity

class CgpaCalculatorActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityCgpaCalculatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCgpaCalculatorBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Ensure the content fills the entire screen, including under the status bar
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val gpaInputs = arrayOf(binding.gpa1, binding.gpa2, binding.gpa3, binding.gpa4, binding.gpa5, binding.gpa6, binding.gpa7, binding.gpa8)
        val calculateBtn = binding.calculateBtn
        val resultText = binding.result

        val weights = doubleArrayOf(0.05, 0.05, 0.10, 0.10, 0.20, 0.20, 0.20, 0.10)

        calculateBtn.setOnClickListener {
            var totalCGPA = 0.0
            var isValid = true

            for (i in gpaInputs.indices) {
                val gpaText = gpaInputs[i].text.toString()
                if (gpaText.isEmpty()) {
                    isValid = false
                    break
                }

                val gpa = gpaText.toDouble()
                if (gpa < 0.0 || gpa > 4.0) {
                    isValid = false
                    break
                }
                totalCGPA += gpa * weights[i]
            }

            if (isValid) {
                resultText.text = "Total CGPA: %.2f".format(totalCGPA)
            } else {
                Toast.makeText(this, "Please enter valid GPAs (0.00 - 4.00) for all semesters", Toast.LENGTH_LONG).show()
            }
        }
    }
}