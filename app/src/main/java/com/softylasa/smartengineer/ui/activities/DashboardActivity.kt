package com.softylasa.smartengineer.ui.activities

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
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.ActivityDashboardBinding
import com.softylasa.smartengineer.ui.fragments.ExploreFragment
import com.softylasa.smartengineer.ui.fragments.LoginFragment
import com.softylasa.smartengineer.ui.fragments.HomeFragment
import com.softylasa.smartengineer.ui.fragments.NoticeFragment

class DashboardActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityDashboardBinding

    // Default value is 1 because 1st tab is selected by default
    private var selectedTab = 0

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
                    tvNotice.visibility = View.GONE
                    tvExplore.visibility = View.GONE

                    ivBook.setImageResource(R.drawable.sl_notebook)
                    ivNotice.setImageResource(R.drawable.sl_academic_cap)
                    ivExplore.setImageResource(R.drawable.sl_library)

                    llBook.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llNotice.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llExplore.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))

                    // select home tab
                    tvHome.visibility = View.VISIBLE
                    ivHome.setImageResource(R.drawable.sl_home_smile_angle_fill)
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
                    true.loadFragment(LoginFragment())

                    //loadFragment(BookFragment.getInstance(DEP_NAME, "MT1"), true);

                    // unselect others tabs expect book tab
                    tvHome.visibility = View.GONE
                    tvNotice.visibility = View.GONE
                    tvExplore.visibility = View.GONE

                    ivHome.setImageResource(R.drawable.sl_home_smile_angle)
                    ivNotice.setImageResource(R.drawable.sl_academic_cap)
                    ivExplore.setImageResource(R.drawable.sl_library)

                    llHome.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llNotice.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llExplore.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))

                    // select book tab
                    tvBook.visibility = View.VISIBLE
                    ivBook.setImageResource(R.drawable.sl_notebook_fill)
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

            llNotice.setOnClickListener{
                // check if profile is already selected or not.
                if (selectedTab != 3) {
                    // set Profile fragment
                    //loadFragment(new MenuFragment(), true);

                    //startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
                    true.loadFragment(NoticeFragment())

                    // unselect others tabs expect notice tab
                    tvHome.visibility = View.GONE
                    tvBook.visibility = View.GONE
                    tvExplore.visibility = View.GONE

                    ivHome.setImageResource(R.drawable.sl_home_smile_angle)
                    ivBook.setImageResource(R.drawable.sl_notebook)
                    ivExplore.setImageResource(R.drawable.sl_library)

                    llHome.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llBook.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llExplore.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))

                    // select profile tab
                    tvNotice.visibility = View.VISIBLE
                    ivNotice.setImageResource(R.drawable.sl_academic_cap_fill)
                    llNotice.setBackgroundResource(R.drawable.round_back_updates_100)

                    // create animation
                    val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f
                    )
                    scaleAnimation.duration = 200
                    scaleAnimation.fillAfter = true
                    llNotice.animation = scaleAnimation

                    // set 4th tab as selected
                    selectedTab = 3
                }
            }

            llExplore.setOnClickListener{

                // check if explore is already selected or not.
                if (selectedTab != 4) {

                    // set explore fragment
                    true.loadFragment(ExploreFragment())

                    //startActivity(new Intent(this, BookViewActivity.class));

                    // unselect others tabs expect explore tab
                    tvHome.visibility = View.GONE
                    tvBook.visibility = View.GONE
                    tvNotice.visibility = View.GONE

                    ivHome.setImageResource(R.drawable.sl_home_smile_angle)
                    ivBook.setImageResource(R.drawable.sl_notebook)
                    ivNotice.setImageResource(R.drawable.sl_academic_cap)

                    llHome.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llBook.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))
                    llNotice.setBackgroundColor(ContextCompat.getColor(this@DashboardActivity, R.color.transparent))

                    // select explore tab
                    tvExplore.visibility = View.VISIBLE
                    ivExplore.setImageResource(R.drawable.sl_library_fill)
                    llExplore.setBackgroundResource(R.drawable.round_back_setting_100)

                    // create animation
                    val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f,
                        Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f
                    )
                    scaleAnimation.duration = 200
                    scaleAnimation.fillAfter = true
                    llExplore.animation = scaleAnimation

                    // set 1st tab as selected
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

        when(fragment){
            is HomeFragment -> ft.setCustomAnimations(R.anim.slide_in_left, R.anim.useless1, R.anim.useless, R.anim.useless)
            is LoginFragment -> ft.setCustomAnimations(R.anim.slide_in_left, R.anim.useless1, R.anim.useless, R.anim.useless)
            is NoticeFragment -> ft.setCustomAnimations(R.anim.slide_in_left, R.anim.useless1, R.anim.useless, R.anim.useless)
            is ExploreFragment -> ft.setCustomAnimations(R.anim.slide_in_right, R.anim.useless, R.anim.useless, R.anim.useless)
        }

        if (!this)  // If Flag is not equal != to True then add Fragment
            ft.add(R.id.navHostDashboardFragmentContainerView, fragment)
        else  // If Flag is equal to True then replace Fragment
            ft.replace(R.id.navHostDashboardFragmentContainerView, fragment)
        ft.commit()
    }
}