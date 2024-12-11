package com.rudhashi.smartengineer.view

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rudhashi.smartengineer.adapter.BookAdapter
import com.rudhashi.smartengineer.databinding.ActivityAllBookBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartThemeActivity

@Suppress("DEPRECATION")
class AllBookActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityAllBookBinding
    private lateinit var bookAdapter: BookAdapter
    private lateinit var query: Query
    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")

    private var rewardedAd: RewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllBookBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        // Set the status bar background to transparent
        window.statusBarColor = Color.TRANSPARENT

        // Ensure the status bar text color is white
        // Clear only the light status bar flag to force white text, without affecting navigation bar
        val flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this)

        // Retrieve data from Intent
        val fieldName = intent.getStringExtra("fieldName")
        val semesterCode = intent.getStringExtra("semesterCode")
        val departmentName = intent.getStringExtra("departmentName")
        val semesterName = intent.getStringExtra("semesterName")
        binding.tvHeaderTitle.text = semesterName
        binding.departmentName.text = departmentName

        // Read eBooks completely free using points
        bookAdapter = BookAdapter { bookCode ->
            redeemPoints(bookCode)
        }

        // Query to filter books where "xSem" contains "AIDT1"
        query = booksCollection.whereArrayContains("$fieldName", "$semesterCode")

        fetchBooks()

        binding.apply {
            rvAllBook.layoutManager = LinearLayoutManager(this@AllBookActivity)
            rvAllBook.adapter = bookAdapter
            btnCourseDetails.setOnClickListener { SmartData.comingSoon(this@AllBookActivity) }
            btnBack.setOnClickListener { finish() }
            btnRefresh.setOnClickListener {
                isShimmerOn(true)
                fetchBooks()
            }
        }
    }

    private fun fetchBooks() {
        // Execute the query
        query.get().addOnSuccessListener { querySnapshot ->
                isShimmerOn(false)
                if (!querySnapshot.isEmpty) {
                    // Iterate over the documents
                    for (document in querySnapshot) {
                        val books = querySnapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                        bookAdapter.submitList(books)
                    }
                } else {
                    Log.d("Firestore", "No books found containing AIDT1 in xSem.")
                }
            }
            .addOnFailureListener { exception ->
                isShimmerOn(false)
                Toast.makeText(this, "Failed to fetch books: $exception", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error fetching data: ", exception)
            }
    }

    override fun onResume() {
        super.onResume()
        // If data is available then Shimmer Effect will be gone
        isShimmerOn(true)
        fetchBooks()

        loadRewardedAd()
    }

    override fun onPause() {
        // Shimmer effect will be stopped when the activity is paused to save battery life.
        // If data is available then Shimmer Effect will be gone
        isShimmerOn(false)
        super.onPause()
    }

    private fun isShimmerOn(flag: Boolean) {
        if (flag) {
            // If data is available then Shimmer Effect will be gone
            binding.shimmerBookList.startShimmer()
            binding.shimmerBookList.visibility = View.VISIBLE
            binding.rvAllBook.visibility = View.GONE
        } else {
            binding.shimmerBookList.stopShimmer()
            binding.shimmerBookList.visibility = View.GONE
            binding.rvAllBook.visibility = View.VISIBLE
        }
    }


    private fun redeemPoints(bookCode: String) {
        val database = Firebase.database
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val userRef = database.getReference("Users/$userId/points")
        userRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
            if (currentPoints >= SmartConfig.REDEEM_POINT) {
                val newPoints = currentPoints - SmartConfig.REDEEM_POINT

                val intent = Intent(this, BookDetailsActivity::class.java)
                userRef.setValue(newPoints)
                intent.putExtra("BOOK_CODE", bookCode)
                startActivity(intent)

            } else {
                showNotEnoughPointsDialog()
                Toast.makeText(this, "Not enough points!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //================================================================

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            SmartConfig.TEST_ADS_ID, // Replace with your Ad Unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Toast.makeText(this@AllBookActivity, "Ad Loaded!", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Toast.makeText(this@AllBookActivity, "Failed to load ad: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd?.show(this) {
                // Reward the user
                val rewardAmount = SmartConfig.WATCH_VIDEO_BONUS_POINT
                rewardAmount.grantUserPoints()
            }
        } else {
            Toast.makeText(this, "Ad not ready yet!", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to grant points
    private fun Int.grantUserPoints() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = Firebase.database.getReference("Users/$userId/points")

        userRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
            val updatedPoints = currentPoints + this
            userRef.setValue(updatedPoints).addOnSuccessListener {
                Toast.makeText(this@AllBookActivity, "You've been rewarded $this points!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNotEnoughPointsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Not Enough Points")
        builder.setMessage("You don't have enough points. Watch a video to get 25 points.")
        builder.setPositiveButton("Watch Video") { _, _ ->
            showRewardedAd() // Show the rewarded ad
        }
        builder.setNegativeButton("Cancel", null)
        builder.create().show()
    }
}