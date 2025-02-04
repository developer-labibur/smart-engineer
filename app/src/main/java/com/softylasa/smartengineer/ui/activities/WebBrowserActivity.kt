package com.softylasa.smartengineer.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.ActivityWebBrowserBinding
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity

class WebBrowserActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityWebBrowserBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWebBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Toolbar setup
        setSupportActionBar(binding.toolbar)

        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set title dynamically based on the intent
        val title = intent.getStringExtra("TITLE") ?: "WebView"
        supportActionBar?.title = title

        // WebView setup
        val url = intent.getStringExtra("URL")
        if (url != null) {
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.webViewClient = WebViewClient()
            binding.webView.loadUrl(url)
        } else {
            finish()
        }

        // Set custom font for the toolbar title
        val typeface = ResourcesCompat.getFont(this, R.font.roboto)
        for (i in 0 until binding.toolbar.childCount) {
            val view = binding.toolbar.getChildAt(i)
            if (view is TextView) {
                view.typeface = typeface
                break
            }
        }

        // Handle back button presses for WebView
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    finish() // Close the activity if the WebView cannot go back
                }
            }
        })
    }

    // Handle toolbar back button press
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}