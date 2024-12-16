package com.rudhashi.smartengineer.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.container.ViewAllBookFragment
import com.rudhashi.smartengineer.container.ViewAllUpdateFragment
import com.rudhashi.smartengineer.databinding.ActivitySmartDashboardBinding
import com.rudhashi.smartengineer.databinding.ActivitySmartViewBinding
import com.rudhashi.smartengineer.fragment.SmartBooksFragment
import com.rudhashi.smartengineer.fragment.SmartHomeFragment
import com.rudhashi.smartengineer.fragment.SmartSettingFragment
import com.rudhashi.smartengineer.fragment.SmartUpdatesFragment
import com.rudhashi.smartengineer.model.User
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartThemeActivity

class SmartViewActivity : SmartThemeActivity() {

    private lateinit var binding: ActivitySmartViewBinding

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private var database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("Users/$userId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartViewBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Ensure the content fills the entire screen, including under the status bar
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewPager() // Setup the viewPager
        setupToolBar()
        setActiveTab(0) // Set the initial tab to Home (index 0)
    }
    // =====================================================================================================================
    // ========================     Setup Bottom Navigation and Main Container (ViewPager)      ============================
    // =====================================================================================================================
    private fun setupViewPager() {
        // Set up ViewPager with fragments
        val fragments = listOf(ViewAllBookFragment(), ViewAllUpdateFragment())

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
    }
    private fun setupToolBar(){

        // Set height based on the currently selected page
        binding.apply {
            // Bottom Navigation Buttons Click Listeners
            btnBook.setOnClickListener { setActiveTab(0) }
            btnUpdate.setOnClickListener { setActiveTab(1) }
            btnPremium.setOnClickListener { SmartData.comingSoon(this@SmartViewActivity) }
            btnBack.setOnClickListener { finish() }

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    setActiveTab(position)
                }
            })
        }
    }
    private fun setActiveTab(position: Int) {
        binding.apply {
            when (position) {
                0 -> {
                    btnBook.setBackgroundResource(R.drawable.smart_view_active_bg)
                    btnUpdate.setBackgroundResource(R.color.transparent)
                }
                1 -> {
                    btnBook.setBackgroundResource(R.color.transparent)
                    btnUpdate.setBackgroundResource(R.drawable.smart_view_active_bg)
                }
                else -> {
                    btnBook.setBackgroundResource(R.drawable.smart_view_active_bg)
                    btnUpdate.setBackgroundResource(R.color.transparent)
                }
            }

            // Sync ViewPager position with selected tab
            if (viewPager.currentItem != position) {
                viewPager.currentItem = position
            }
        }
    }
    private fun displayPointsDynamically() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convert snapshot to User object
                val user = snapshot.getValue(User::class.java)
                binding.tvPremium.text = user?.points.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                SmartData.sToast(this@SmartViewActivity, "Failed to load points")
            }
        })
    }
    override fun onResume() {
        super.onResume()
        displayPointsDynamically()

        val displayFragment = intent.getStringExtra("DISPLAY").toString()
        when (displayFragment) {
            "Book" -> setActiveTab(0)
            "Update" -> setActiveTab(1)
        }
    }
}