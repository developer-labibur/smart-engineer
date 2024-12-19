package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.BookAdapter
import com.rudhashi.smartengineer.databinding.ActivityAllBookBinding
import com.rudhashi.smartengineer.databinding.DialogCongratulatuinBinding
import com.rudhashi.smartengineer.databinding.DialogWatchVideoBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.model.Semester
import com.rudhashi.smartengineer.model.User
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartThemeActivity

class AllBookActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityAllBookBinding
    private lateinit var bookAdapter: BookAdapter
    private lateinit var query: Query
    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")

    private lateinit var fieldName: String
    private lateinit var departmentAbbr: String
    private lateinit var semesterNo: String
    private lateinit var abbr: String
    private lateinit var isCST: String
    private lateinit var no: String
    private lateinit var course: String
    private lateinit var departmentName: String
    private lateinit var semesterName: String

    private var rewardedAd: RewardedAd? = null
    private var watchVideoBonus = SmartConfig.WATCH_VIDEO_BONUS_POINT
    private var tag = "AllBookActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllBookBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve data from Intent
        fieldName = intent.getStringExtra("fieldName").toString()
        departmentAbbr = intent.getStringExtra("departmentAbbr").toString()
        isCST = intent.getStringExtra("abbr").toString()
        abbr = if (isCST == "CST") "CMT" else isCST
        no = intent.getStringExtra("no").toString()
        course = no + abbr
        semesterNo = intent.getStringExtra("semesterNo").toString()
        departmentName = intent.getStringExtra("departmentName").toString()
        semesterName = intent.getStringExtra("semesterName").toString()
        binding.tvHeaderTitle.text = semesterName
        binding.departmentName.text = departmentName

        // Read eBooks completely free using points
        bookAdapter = BookAdapter { bookCode ->
            redeemPoints(bookCode)
        }

        // Query to filter books where "xSem" contains "AIDT1"
        query = booksCollection.whereArrayContains(fieldName, departmentAbbr)

        fetchBooks()
        fetchCourseStructure()
        if (SmartConfig.isAdsVisible(this)) loadRewardedAd()

        binding.apply {
            rvAllBook.layoutManager = LinearLayoutManager(this@AllBookActivity)
            rvAllBook.adapter = bookAdapter
            btnBack.setOnClickListener { finish() }
            btnPremium.setOnClickListener { startActivity(Intent(this@AllBookActivity, SmartPointActivity::class.java)) }
        }
    }

    private fun fetchCourseStructure(){
        db.collection("semester").document(course).get().addOnSuccessListener {querySnapshot ->
            // Handle the retrieved document
            val semester = querySnapshot.toObject(Semester::class.java)
            val courseStructure = semester?.SPdf
            if (courseStructure != null) {
                binding.btnCourseDetails.setOnClickListener {
                    val intent = Intent(this, SmartEBookActivity::class.java)
                    intent.putExtra("UName", "$departmentName ($semesterName) - Course Structure & Information")
                    intent.putExtra("UPdf", courseStructure)
                    intent.putExtra("UNo", "1")
                    intent.putExtra("BCode", "N/A")
                    intent.putExtra("BName", "N/A")
                    intent.putExtra("Key", "notBook")
                    startActivity(intent)
                }
            } else{
                binding.btnCourseDetails.setOnClickListener { SmartData.comingSoon(this@AllBookActivity) }
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
        displayPointsDynamically()
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
            if (SmartConfig.isAdsVisible(this)){
                if (currentPoints >= SmartConfig.REDEEM_POINT) {
                    val newPoints = currentPoints - SmartConfig.REDEEM_POINT

                    val intent = Intent(this, BookDetailsActivity::class.java)
                    userRef.setValue(newPoints)
                    intent.putExtra("BOOK_CODE", bookCode)
                    startActivity(intent)

                } else {
                    showNotEnoughPointsDialog()
                }
            } else {
                if (currentPoints >= SmartConfig.REDEEM_POINT) {
                    val newPoints = currentPoints - SmartConfig.REDEEM_POINT

                    val intent = Intent(this, BookDetailsActivity::class.java)
                    userRef.setValue(newPoints)
                    intent.putExtra("BOOK_CODE", bookCode)
                    startActivity(intent)

                } else {
                    val intent = Intent(this, BookDetailsActivity::class.java)
                    intent.putExtra("BOOK_CODE", bookCode)
                    startActivity(intent)
                }
            }
        }
    }
    @SuppressLint("InflateParams")
    private fun showNotEnoughPointsDialog() {
        val builder = AlertDialog.Builder(this).create()
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_watch_video, null)
        val dialogBinding = DialogWatchVideoBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        val firstText = "You don't have enough points. Watch a video to get"
        val rewardText = "$watchVideoBonus Smart Points"
        val fullText = "$firstText $rewardText" // Combine them into one string
        val spannableString = SpannableString(fullText) // Create a SpannableString
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD), // Bold style
            firstText.length + 1, // Start index (include the space after firstText)
            fullText.length, // End index (end of the string)
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        dialogBinding.tvWatchInfo.text = spannableString // Set the SpannableString to the TextView
        dialogBinding.btnWatchAds.setOnClickListener {
            showRewardedAd() // Show the rewarded ad
            builder.dismiss()
        }
        dialogBinding.btnClose.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(
            ColorDrawable(0)
        )
        builder.show()
    }
    @SuppressLint("InflateParams")
    private fun showCongratulationDialog() {
        val builder = AlertDialog.Builder(this).create()
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_congratulatuin, null)
        val dialogBinding = DialogCongratulatuinBinding.bind(dialogView)
        builder.setView(dialogBinding.root)
        builder.setCanceledOnTouchOutside(false)
        builder.show()

        dialogBinding.tv25Point.text = getString(R.string._smart_points, watchVideoBonus.toString())
        dialogBinding.btnSmartPoint.setOnClickListener {builder.dismiss() }
        dialogBinding.btnClose.setOnClickListener { builder.dismiss() }

        if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
        builder.show()
    }

    //================================================================
    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            SmartConfig.getRewardedAdsId(),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }
    private fun showRewardedAd() {
        if (rewardedAd == null) {
            return
        }

        rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(tag, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(tag, "Ad dismissed fullscreen content.")
                rewardedAd = null
                showCongratulationDialog()
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                Log.e(tag, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(tag, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(tag, "Ad showed fullscreen content.")
            }
        }

        rewardedAd?.let { ad ->
            ad.show(this) { _ ->
                // Handle the reward.
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val userRef = Firebase.database.getReference("Users/$userId/points")
                //val rewardAmount = rewardItem.amount
                //val rewardType = rewardItem.type

                // Function to grant points
                userRef.get().addOnSuccessListener { snapshot ->
                    val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
                    val updatedPoints = currentPoints + watchVideoBonus
                    userRef.setValue(updatedPoints).addOnSuccessListener {
                        Log.d(tag, "User earned the reward.")
                    }
                }
            }
        } ?: run {
            Log.d(tag, "The rewarded ad wasn't ready yet.")
        }

    }

    // =================================================================
    private fun displayPointsDynamically() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseDatabase.getInstance().getReference("Users/$userId")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convert snapshot to User object
                val user = snapshot.getValue(User::class.java)
                binding.tvPremium.text = user?.points.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AllBookActivity,
                    "Failed to load points",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}