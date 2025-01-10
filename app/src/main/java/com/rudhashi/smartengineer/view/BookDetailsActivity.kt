package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.WindowMetrics
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdSize
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.UnitAdapter
import com.rudhashi.smartengineer.databinding.ActivityBookDetailsBinding
import com.rudhashi.smartengineer.manager.AdManager
import com.rudhashi.smartengineer.manager.CUserManager
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.model.BookUnit
import com.rudhashi.smartengineer.model.CategoryDataClass
import com.rudhashi.smartengineer.model.Semester
import com.rudhashi.smartengineer.util.ConvertFB
import com.rudhashi.smartengineer.util.PdfUtils
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartThemeActivity
import java.io.File
import kotlin.properties.Delegates

class BookDetailsActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityBookDetailsBinding
    private lateinit var db: FirebaseFirestore

    // Update type for the unit list and adapter
    private val unitList = mutableListOf<BookUnit>()
    private val tagInfo = "BookDetailsActivity"
    private var isFullDes by Delegates.notNull<Boolean>()

    private lateinit var bookCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Ensure the content fills the entire screen, including under the status bar
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        AdManager.setBannerAds(this, binding.adViewContainer, adSize, true)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Get Book Code from intent and load units
        bookCode = intent.getStringExtra("BOOK_CODE").toString()
        binding.unitRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize RecyclerView
        loadBooksAndUnits(bookCode)
        fetchBookDetails(bookCode) // Fetch the book details including xDep and xSem
        updateBookViewCount(bookCode) // Call this function to handle the view count

        CUserManager.points { binding.tvPoint.text = "$it" }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnPremium.setOnClickListener { startActivity(Intent(this, SmartPointActivity::class.java)) }
    }

    private fun downloadPdf(bookUnit: BookUnit) {
        val cachedFile = getCachedPdfFile(bookUnit.UName)
        if (cachedFile.exists()) {
            Toast.makeText(this, "Already downloaded", Toast.LENGTH_SHORT).show()
        } else {
            PdfUtils.downloadPdf(this, bookUnit.UPdf, cachedFile) {
                Toast.makeText(this, "Downloaded ${bookUnit.UName}", Toast.LENGTH_SHORT).show()
                binding.unitRecyclerView.adapter?.notifyDataSetChanged() // Refresh RecyclerView
            }
        }
    }

    private fun getCachedPdfFile(unitName: String): File {
        return File(cacheDir, "$unitName.pdf")
    }


    private fun loadBooksAndUnits(bookCode: String) {
        db.collection("books").document(bookCode)
            .get()
            .addOnSuccessListener { snapshot ->
                val book = snapshot.toObject(Book::class.java)
                val bName = book?.BName
                val bCode = book?.BCode
                val bCredit = book?.BCredit
                binding.apply {
                    Glide.with(this@BookDetailsActivity)
                        .load(book?.BCover)
                        .into(bookCoverImage)
                    bookCodes.text = getString(R.string.book_name_n_code, bName, bCode)
                    bookTotalUnits.text = book?.BTotalU
                    "Credit $bCredit".also { viewCountTextCredit.text = it }

                    val fullDescription = book?.BDes ?: ""
                    val limitedDescription = fullDescription.split(" ").take(30).joinToString(" ") // First 30 words
                    val clickToSeeMore = "...See More"
                    val collapsedText = "$limitedDescription $clickToSeeMore"

                    // Set initial state
                    isFullDes = false
                    if (fullDescription.isNotEmpty()){
                        tvInfo.text = SpannableString(collapsedText).apply {
                            setSpan(
                                StyleSpan(Typeface.BOLD),
                                length - clickToSeeMore.length,
                                length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        // Handle text toggle on click
                        tvInfo.setOnClickListener {
                            if (isFullDes) {
                                // Collapse the text
                                tvInfo.text = SpannableString(collapsedText).apply {
                                    setSpan(StyleSpan(Typeface.BOLD), length - clickToSeeMore.length, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                }
                            } else {
                                // Expand the text
                                tvInfo.text = fullDescription
                            }
                            isFullDes = !isFullDes // Toggle state
                        }
                    } else {
                        tvInfo.text = getString(R.string.book_details_is_empty)
                    }
                }
                isShimmerOn(false)
            }
            .addOnFailureListener { exception ->
                Log.e(tagInfo, "Error loading units: ", exception)
                isShimmerOn(false)
            }

        // Load the units of the book ----------------------------------------------------------------
        db.collection("books").document(bookCode).collection("BUnit")
            .orderBy("UNo", Query.Direction.ASCENDING) // Or DESCENDING for reverse order
            .get()
            .addOnSuccessListener { documents ->
                parseUnitData(documents)
                isShimmerOn(false)
            }
            .addOnFailureListener { exception ->
                Log.e(tagInfo, "Error loading units: ", exception)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun parseUnitData(documents: QuerySnapshot) {
        // Parsing Firestore data and adding it to the list ----------------------------------------------------------------
        unitList.clear() // Clear the list to avoid duplicates
        for (document in documents) {
            val unit = document.toObject(BookUnit::class.java)
            unitList.add(unit)
        }
        binding.unitRecyclerView.adapter = UnitAdapter(this, unitList, bookCode) { bookUnit, action ->
            when (action) {
                "view" -> openSmartEBookActivity(bookUnit)
                "download" -> loadBooksAndUnits(bookCode)
            }
        }
        //unitAdapter.notifyDataSetChanged() // Refresh adapter to display new data
    }

    private fun openSmartEBookActivity(bookUnit: BookUnit) {
        val intent = Intent(this, SmartEBookActivity::class.java)
        intent.putExtra("UName", bookUnit.UName)
        intent.putExtra("UPdf", bookUnit.UPdf)
        startActivity(intent)
    }

    private fun fetchBookDetails(bookCode: String) {
        db.collection("books").document(bookCode).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val semester = document["xSem"] as? List<String> ?: listOf()

                    val book = document.toObject(Book::class.java)
                    if (book != null) {
                        // Now, xDep is an array of Strings
                        val departmentCodes = book.xDep
                        val departmentNames = departmentCodes.map {
                            ConvertFB.covertDepCodeToName(it)
                        }

                        displayDepartmentTags(departmentNames)
                        displaySemesterTags(semester)
                    }
                } else {
                    Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to fetch book details: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun displayDepartmentTags(departmentNames: List<String>) {
        binding.tagContainerXDep.removeAllViews() // Clear any existing views

        // Loop through the department names and create TextViews
        departmentNames.forEach { departmentName ->
            val textView = TextView(this@BookDetailsActivity).apply {
                text = departmentName
                StyleSpan(Typeface.BOLD)
                setBackgroundResource(R.drawable.tag_background)  // Set custom background
                setPadding(16, 8, 16, 5)  // Set padding (left, top, right, bottom)
                typeface = ResourcesCompat.getFont(context, R.font.main_font_regular_2)
                setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.black)) // Set text color

                // Add OnClickListener to start new activity
                setOnClickListener {
                    val depCode = ConvertFB.covertDepartmentToCode(departmentName)
                    db.collection("departments")
                        .document(depCode)
                        .get().addOnSuccessListener {
                            val department = it.toObject<CategoryDataClass>()
                            val intent = Intent(this@BookDetailsActivity, DepartmentActivity::class.java)
                            intent.putExtra("DCode", department?.DCode)
                            startActivity(intent)
                        }
                }
            }

            // Create LayoutParams with margins (left, top, right, bottom)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,  // Width
                LinearLayout.LayoutParams.WRAP_CONTENT   // Height
            ).apply {
                // Add margins to the TextView
                setMargins(
                    SmartData.dpToPx(this@BookDetailsActivity, 3f),
                    SmartData.dpToPx(this@BookDetailsActivity, 5f),
                    SmartData.dpToPx(this@BookDetailsActivity, 3f),
                    SmartData.dpToPx(this@BookDetailsActivity, 5f)
                )  // (left, top, right, bottom) margins in pixels
            }

            // Set LayoutParams to the TextView
            textView.layoutParams = params

            // Add the TextView to your layout (assuming `linearLayout` is your container)
            binding.tagContainerXDep.addView(textView)
        }
    }

    private fun displaySemesterTags(semesterNames: List<String>) {
        binding.tagContainerXSem.removeAllViews() // Clear any existing views

        // Loop through the department names and create TextViews
        semesterNames.forEach { semName ->
            val textView = TextView(this@BookDetailsActivity).apply {
                text = semName
                StyleSpan(Typeface.BOLD)
                setBackgroundResource(R.drawable.tag_background)  // Set custom background
                setPadding(16, 8, 16, 5)  // Set padding (left, top, right, bottom)
                typeface = ResourcesCompat.getFont(context, R.font.main_font_regular_2)
                setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.black)) // Set text color

                // Add OnClickListener to start new activity
                setOnClickListener {
                    db.collection("semester")
                        .get().addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot.documents) {
                                val semester = document.toObject<Semester>()
                                val semAbbr = semester?.STag
                                if (semAbbr == semName) {
                                    val intent = Intent(this@BookDetailsActivity, AllBookActivity::class.java)
                                    val dAbbr = semester.SAbbr
                                    val semNumber = semester.SSem
                                    intent.putExtra("abbrNo", "${dAbbr}$semNumber")
                                    intent.putExtra("noAbbr", "${semNumber}$dAbbr")
                                    intent.putExtra("departmentName", semester.SDep)
                                    intent.putExtra("semesterName", semester.SNo)
                                    startActivity(intent)
                                }
                            }
                        }
                }
            }

            // Create LayoutParams with margins (left, top, right, bottom)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,  // Width
                LinearLayout.LayoutParams.WRAP_CONTENT   // Height
            ).apply {
                // Add margins to the TextView
                setMargins(
                    SmartData.dpToPx(this@BookDetailsActivity, 3f),
                    SmartData.dpToPx(this@BookDetailsActivity, 5f),
                    SmartData.dpToPx(this@BookDetailsActivity, 3f),
                    SmartData.dpToPx(this@BookDetailsActivity, 5f)
                )  // (left, top, right, bottom) margins in pixels
            }

            // Set LayoutParams to the TextView
            textView.layoutParams = params

            // Add the TextView to your layout (assuming `linearLayout` is your container)
            binding.tagContainerXSem.addView(textView)
        }
    }

    private fun updateBookViewCount(bookCode: String) {
        // Reference to the specific book document in Firestore
        val bookRef = db.collection("books").document(bookCode)

        // Fetch current BView count
        bookRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Get the current view count, default to 0 if it doesn't exist
                val currentViewCount = document.getLong("BView") ?: 0

                // Increment the view count
                val newViewCount = currentViewCount + 1

                // Update the BView field in Firestore
                bookRef.update("BView", newViewCount)
                    .addOnSuccessListener {
                        // Optionally, display the new view count in the UI
                        binding.viewCountTextView.text = getString(R.string.reads, newViewCount.toString())
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Failed to update view count: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error fetching book data: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        val bookCode = intent.getStringExtra("BOOK_CODE")
        // If data is available then Shimmer Effect will be gone
        isShimmerOn(true)
        bookCode?.let { loadBooksAndUnits(it) }
        bookCode?.let { fetchBookDetails(it) } // Fetch the book details including xDep and xSem
        bookCode?.let { updateBookViewCount(it) } // Call this function to handle the view count
    }

    override fun onPause() {
        // Shimmer effect will be stopped when the activity is paused to save battery life.
        // If data is available then Shimmer Effect will be gone
        isShimmerOn(false)
        super.onPause()
    }
    override fun onDestroy() {
        binding.adViewContainer.destroy() // Avoid memory leaks
        super.onDestroy()
    }

    private fun isShimmerOn(flag: Boolean) {
        if (flag) {
            // If data is available then Shimmer Effect will be gone
            binding.shimmerDetails.startShimmer()
            binding.shimmerDetails.visibility = View.VISIBLE
            binding.bookDetails.visibility = View.GONE
        } else {
            binding.shimmerDetails.stopShimmer()
            binding.shimmerDetails.visibility = View.GONE
            binding.bookDetails.visibility = View.VISIBLE
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