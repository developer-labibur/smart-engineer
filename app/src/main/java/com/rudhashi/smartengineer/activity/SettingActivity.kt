package com.rudhashi.smartengineer.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rudhashi.smartengineer.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the binding

        // Set the switch listener
        binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this@SettingActivity, "Night Mode On", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SettingActivity, "Day Mode On", Toast.LENGTH_SHORT).show()
            }
        }
    }
}