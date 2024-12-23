package com.rudhashi.smartengineer.page

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rudhashi.smartengineer.databinding.ActivityAboutUsBinding
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartThemeActivity

class AboutUsActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            linkFB.setOnClickListener {
                openFacebookProfile("https://www.facebook.com/labib.2028", "100023981505808")
            }
        }
    }

    private fun openFacebookProfile(profileUrl: String, profileId: String) {
        try {
            val intentList = ArrayList<Intent>()

            // Intent for Facebook App
            val fbIntent = Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/$profileId"))
            fbIntent.`package` = "com.facebook.katana"
            intentList.add(fbIntent)

            // Intent for Facebook Lite
            val liteIntent = Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/$profileId"))
            liteIntent.`package` = "com.facebook.lite"
            intentList.add(liteIntent)

            // Create chooser dialog
            val chooser = Intent.createChooser(Intent(), "Open with")
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())

            startActivity(chooser)

        } catch (e: PackageManager.NameNotFoundException) {
            // Facebook app not installed, open URL in browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle other exceptions
            SmartData.sToast(this, "Unable to open Facebook")
        }
    }

}