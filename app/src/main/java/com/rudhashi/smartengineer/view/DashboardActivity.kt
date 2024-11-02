package com.rudhashi.smartengineer.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ActivityDashboardBinding
import com.rudhashi.smartengineer.fragment.HomeFragment
import com.rudhashi.smartengineer.fragment.LoginFragment
import com.rudhashi.smartengineer.fragment.SignupFragment
import com.rudhashi.smartengineer.fragment.TechFragment

class DashboardActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityDashboardBinding

    // Default value is 1 because 1st tab is selected by default
    private var selectedTab = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Completely transparent Status Bar -------------------------------------------------------------------------------
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.apply {

            llHome.setOnClickListener{
                // check if home is already selected or not.
                if (selectedTab != 1) {

                    // set home fragment
                    true.loadFragment(HomeFragment())

                    // unselect others tabs expect home tab
                    tvBook.visibility = View.GONE
                    tvExplore.visibility = View.GONE
                    tvProfile.visibility = View.GONE

                    ivBook.setImageResource(R.drawable.bottom_book)
                    ivExplore.setImageResource(R.drawable.bottom_explore)
                    ivProfile.setImageResource(R.drawable.bottom_profile)

                    llBook.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llBook.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llExplore.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llProfile.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))

                    // select home tab
                    tvHome.visibility = View.VISIBLE
                    ivHome.setImageResource(R.drawable.bottom_selected_home)
                    llHome.setBackgroundResource(R.drawable.round_back_home_100)

                    // create animation
                    val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f
                    )
                    scaleAnimation.duration = 200
                    scaleAnimation.fillAfter = true
                    llHome.animation = scaleAnimation

                    // set 1st tab as selected
                    selectedTab = 1
                }
            }

            llBook.setOnClickListener{

                // check if book is already selected or not.
                if (selectedTab != 2) {

                    // set Book fragment
                    true.loadFragment(SignupFragment())

                    //loadFragment(BookFragment.getInstance(DEP_NAME, "MT1"), true);

                    // unselect others tabs expect book tab
                    tvHome.visibility = View.GONE
                    tvExplore.visibility = View.GONE
                    tvProfile.visibility = View.GONE

                    ivHome.setImageResource(R.drawable.bottom_home)
                    ivExplore.setImageResource(R.drawable.bottom_explore)
                    ivProfile.setImageResource(R.drawable.bottom_profile)

                    llHome.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llExplore.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llProfile.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))

                    // select book tab
                    tvBook.visibility = View.VISIBLE
                    ivBook.setImageResource(R.drawable.bottom_selected_book)
                    llBook.setBackgroundResource(R.drawable.round_back_book_100)

                    // create animation
                    val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f
                    )
                    scaleAnimation.duration = 200
                    scaleAnimation.fillAfter = true
                    llBook.animation = scaleAnimation

                    // set 2nd tab as selected
                    selectedTab = 2
                }
            }

            llExplore.setOnClickListener{

                // check if explore is already selected or not.
                if (selectedTab != 3) {

                    // set explore fragment
                    true.loadFragment(LoginFragment())

                    //startActivity(new Intent(this, BookViewActivity.class));

                    // unselect others tabs expect explore tab
                    tvHome.visibility = View.GONE
                    tvBook.visibility = View.GONE
                    tvProfile.visibility = View.GONE

                    ivHome.setImageResource(R.drawable.bottom_home)
                    ivBook.setImageResource(R.drawable.bottom_book)
                    ivProfile.setImageResource(R.drawable.bottom_profile)

                    llHome.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llBook.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llProfile.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))

                    // select explore tab
                    tvExplore.visibility = View.VISIBLE
                    ivExplore.setImageResource(R.drawable.bottom_selected_explore)
                    llExplore.setBackgroundResource(R.drawable.round_back_explore_100)

                    // create animation
                    val scaleAnimation = ScaleAnimation(
                        0.8f,
                        1.0f,
                        1f,
                        1f,
                        Animation.RELATIVE_TO_SELF,
                        0.0f,
                        Animation.RELATIVE_TO_SELF,
                        0.0f
                    )
                    scaleAnimation.duration = 200
                    scaleAnimation.fillAfter = true
                    llExplore.animation = scaleAnimation

                    // set 1st tab as selected
                    selectedTab = 3
                }
            }

            llProfile.setOnClickListener{
                // check if profile is already selected or not.
                if (selectedTab != 4) {
                    // set Profile fragment
                    //loadFragment(new MenuFragment(), true);

                    //startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
                    true.loadFragment(TechFragment())

                    // unselect others tabs expect profile tab
                    tvHome.visibility = View.GONE
                    tvBook.visibility = View.GONE
                    tvExplore.visibility = View.GONE

                    ivHome.setImageResource(R.drawable.bottom_home)
                    ivBook.setImageResource(R.drawable.bottom_book)
                    ivExplore.setImageResource(R.drawable.bottom_explore)

                    llHome.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llBook.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llExplore.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))

                    // select profile tab
                    tvProfile.visibility = View.VISIBLE
                    ivProfile.setImageResource(R.drawable.bottom_selected_profile)
                    llProfile.setBackgroundResource(R.drawable.round_back_profile_100)

                    // create animation
                    val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f,
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f
                    )
                    scaleAnimation.duration = 200
                    scaleAnimation.fillAfter = true
                    llProfile.animation = scaleAnimation

                    // set 4th tab as selected
                    selectedTab = 4
                }
            }
        }

    }

    // ----------------------------------------------------------------
    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.navHostDashboardFragmentContainerView)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    //#############################################################################################
    private fun Boolean.loadFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        if (!this)  // If Flag is not equal != to True then add Fragment
            ft.add(R.id.navHostDashboardFragmentContainerView, fragment)
        else  // If Flag is equal to True then replace Fragment
            ft.replace(R.id.navHostDashboardFragmentContainerView, fragment)
        ft.commit()
    }
}