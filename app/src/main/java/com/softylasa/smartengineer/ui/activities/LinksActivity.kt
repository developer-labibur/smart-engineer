package com.softylasa.smartengineer.ui.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.models.Links
import com.softylasa.smartengineer.databinding.ActivityLinksBinding
import com.softylasa.smartengineer.ui.adapters.LinkAdapter
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity

class LinksActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityLinksBinding

    private lateinit var linkAdapter: LinkAdapter
    private val linkList = mutableListOf<Links>()

    private lateinit var name: String
    private lateinit var database: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLinksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        name = intent.getStringExtra("name").toString()
        database = intent.getStringExtra("database").toString()

        linkAdapter = LinkAdapter(linkList, this)
        binding.recyclerView.adapter = linkAdapter

        // Set a listener for item clicks (to open Chrome Custom Tab with link)
        linkAdapter.setOnItemClickListener(object : LinkAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, link: String) {
                openChromeCustomTab(link)
            }
        })

        binding.tvHeading.text = name

        // Fetch data from Firestore
        fetchDataFromFirestore(database)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchDataFromFirestore(database: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(database).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val socialLink = document.toObject(Links::class.java)
                    linkList.add(socialLink)
                }
                linkAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("LinksActivity", "Error getting documents: ${exception.message}")
            }
    }

    private fun openChromeCustomTab(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true) // Shows the page title
            .setToolbarColor(ContextCompat.getColor(this, R.color.custom_tab_color)) // Change toolbar color
            .setUrlBarHidingEnabled(true) // Hides URL bar for a cleaner UI
            .setShareState(CustomTabsIntent.SHARE_STATE_ON) // Enables share button
            .setStartAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right) // Smooth transition
            .setExitAnimations(this, android.R.anim.fade_in, android.R.anim.fade_out)
            .build()

        val uri = Uri.parse(url)

        try {
            customTabsIntent.intent.putExtra("android.support.customtabs.extra.TOOLBAR_COLOR", ContextCompat.getColor(this, R.color.custom_tab_color))
            customTabsIntent.intent.putExtra("android.support.customtabs.extra.SECONDARY_TOOLBAR_COLOR", ContextCompat.getColor(this, R.color.custom_tab_color))

            // Make it open as a bottom sheet
            customTabsIntent.intent.putExtra("androidx.browser.customtabs.extra.SHARE_STATE", CustomTabsIntent.SHARE_STATE_OFF)
            customTabsIntent.intent.putExtra("androidx.browser.customtabs.extra.ACTIVITY_HEIGHT_RESIZE_BEHAVIOR", 1) // Enable bottom-sheet mode

            customTabsIntent.launchUrl(this, uri)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open Chrome Custom Tab", Toast.LENGTH_SHORT).show()
        }
    }
}