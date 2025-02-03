package com.rudhashi.smartengineer.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowMetrics
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdSize
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rudhashi.smartengineer.ui.adapters.BookListAdapter
import com.rudhashi.smartengineer.databinding.ActivityAllBookBinding
import com.rudhashi.smartengineer.managers.AdManager
import com.rudhashi.smartengineer.data.models.Book
import com.rudhashi.smartengineer.data.models.Semester
import com.rudhashi.smartengineer.managers.CUserManager
import com.rudhashi.smartengineer.managers.PointManager
import com.rudhashi.smartengineer.utils.helpers.SmartData
import com.rudhashi.smartengineer.utils.themes.SmartThemeActivity

class AllBookActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityAllBookBinding
    private lateinit var bookAdapter: BookListAdapter
    private lateinit var query: Query
    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")

    private lateinit var departmentName: String
    private lateinit var semesterName: String
    private lateinit var noAbbr: String
    private lateinit var abbrNo: String

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
        departmentName = intent.getStringExtra("departmentName").toString()
        semesterName = intent.getStringExtra("semesterName").toString()
        abbrNo = intent.getStringExtra("abbrNo").toString()
        noAbbr = intent.getStringExtra("noAbbr").toString()

        // Read eBooks completely free using points
        bookAdapter = BookListAdapter { bookCode ->
            PointManager.redeemPointsOpenEBook(this@AllBookActivity, bookCode)
        }

        binding.tvHeaderTitle.text = semesterName
        binding.departmentName.text = departmentName

        // Query to filter books where "xSem" contains "AIDT1"
        query = booksCollection.whereArrayContains("xSem", abbrNo)

        fetchBooks()
        fetchCourseStructure()
        AdManager.setBannerAds(this, binding.adViewContainer, adSize, false)

        binding.apply {
            CUserManager.points { tvPoint.text = "$it" }
            rvAllBook.layoutManager = LinearLayoutManager(this@AllBookActivity)
            rvAllBook.adapter = bookAdapter
            btnBack.setOnClickListener { finish() }
            btnPremium.setOnClickListener { startActivity(Intent(this@AllBookActivity, SmartPointActivity::class.java)) }
        }
    }

    private fun fetchCourseStructure(){
        db.collection("semester").document(noAbbr)
            .get().addOnSuccessListener {querySnapshot ->
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
                    PointManager.redeemPointsOpenPDF()
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
                        bookAdapter.updateBooks(books, false)
                    }
                }
            }
            .addOnFailureListener { exception ->
                isShimmerOn(false)
                SmartData.sToast(this, "Failed to fetch books: $exception")
                Log.e("Firestore", "Error fetching data: ", exception)
            }
    }

    override fun onResume() {
        super.onResume()
        // If data is available then Shimmer Effect will be gone
        isShimmerOn(true)
        fetchBooks()
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

    // Get the ad size with screen width.
    private val adSize: AdSize
        get() {
            val displayMetrics = resources.displayMetrics
            val adWidthPixels =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics: WindowMetrics = this.windowManager.currentWindowMetrics
                    windowMetrics.bounds.width()
                } else {
                    displayMetrics.widthPixels
                }
            val density = displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

}