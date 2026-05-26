package com.softylasa.smartengineer.ui.activities

import android.os.Bundle
import android.view.ViewGroup
import com.softylasa.smartengineer.databinding.ActivityTestBinding
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity
class TestActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.crush.text = "Test Crash"
        binding.crush.setOnClickListener {
            throw RuntimeException("Test Crash") // Force a crash
        }

        addContentView(binding.crush, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT))

    }
}