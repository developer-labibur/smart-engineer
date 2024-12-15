package com.rudhashi.smartengineer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.CategoryAdapter
import com.rudhashi.smartengineer.adapter.UnitAdapter
import com.rudhashi.smartengineer.databinding.ActivityBookDetailsBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.model.BookUnit
import com.rudhashi.smartengineer.model.CategoryDataClass
import com.rudhashi.smartengineer.model.Semester
import com.rudhashi.smartengineer.model.User
import com.rudhashi.smartengineer.util.ConvertFB
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartThemeActivity
import kotlin.properties.Delegates

class BookDetailsActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityBookDetailsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var unitAdapter: UnitAdapter

    // Update type for the unit list and adapter
    private val unitList = mutableListOf<BookUnit>()
    private val tagInfo = "BookDetailsActivity"
    private var isFullDes by Delegates.notNull<Boolean>()

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private var database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("Users/$userId")

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

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Get Book Code from intent and load units
        val bookCode = intent.getStringExtra("BOOK_CODE")

        if (bookCode != null) {
            // Initialize RecyclerView
            setupRecyclerView(bookCode)

            loadBooksAndUnits(bookCode)
            fetchBookDetails(bookCode) // Fetch the book details including xDep and xSem
            updateBookViewCount(bookCode) // Call this function to handle the view count
        } else {
            Log.e(tagInfo, "Book Code is null")
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView(bookCode: String) {
        // Set up the RecyclerView with UnitAdapter
        unitAdapter = UnitAdapter(this, unitList, bookCode)
        binding.unitRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BookDetailsActivity)
            adapter = unitAdapter
        }
    }

    private fun loadBooksAndUnits(bookCode: String) {
        // Load the book details ----------------------------------------------------------------
        db.collection("books").document(bookCode)
            .get()
            .addOnSuccessListener { snapshot ->
                val book = snapshot.toObject(Book::class.java)
                binding.apply {
                    Glide.with(this@BookDetailsActivity)
                        .load(book?.BCover)
                        .placeholder(R.drawable.book)
                        .into(bookCoverImage)
                    tvBookTitle.text = book?.BName
                    bookCodes.text = getString(R.string.sub_code, book?.BCode)
                    bookTotalUnits.text = book?.BTotalU
                    val des = book?.BDes
                    isFullDes = false
                    if (isFullDes){
                        tvInfo.text = des  // Set the spannable string to the TextView
                        isFullDes = false
                    } else{
                        val limitedText = des?.split(" ")?.take(30)?.joinToString(" ") // Limit to the first 3 words
                        val clickToSeeMore = "...See More"
                        val fullText = "$limitedText $clickToSeeMore" // Combine them into one string
                        val spannableString = SpannableString(fullText) // Create a SpannableString
                        // Set the "...See More" part in bold
                        spannableString.setSpan(
                            StyleSpan(Typeface.BOLD),
                            fullText.length - clickToSeeMore.length, // Start index of "...Click to See More"
                            fullText.length, // End index of "...Click to See More"
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        tvInfo.text = spannableString  // Set the spannable string to the TextView
                        isFullDes = true
                    }

                    val date = book?.TimeStamp?.let { SmartData.timestampToDate(it) }
                    dataUploaded.text = date
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
        unitAdapter.notifyDataSetChanged() // Refresh adapter to display new data
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
                setBackgroundResource(R.drawable.tag_background)  // Set custom background
                setPadding(16, 8, 16, 5)  // Set padding (left, top, right, bottom)
                typeface = ResourcesCompat.getFont(context, R.font.main_font_regular_2)
                setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.white)) // Set text color

                // Add OnClickListener to start new activity
                setOnClickListener {
                    val depCode = ConvertFB.covertDepartmentToCode(departmentName)
                    db.collection("departments")
                        .document(depCode)
                        .get().addOnSuccessListener {
                            val department = it.toObject<CategoryDataClass>()
                            val intent = Intent(this@BookDetailsActivity, DepartmentActivity::class.java)
                            intent.putExtra("DAbbr", department?.DAbbr)
                            intent.putExtra("DCode", department?.DCode)
                            intent.putExtra("DCover", department?.DCover)
                            intent.putExtra("DInfo", department?.DInfo)
                            intent.putExtra("DName", department?.DName)
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
                setBackgroundResource(R.drawable.tag_background)  // Set custom background
                setPadding(16, 8, 16, 5)  // Set padding (left, top, right, bottom)
                typeface = ResourcesCompat.getFont(context, R.font.main_font_regular_2)
                setTextColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.white)) // Set text color

                // Add OnClickListener to start new activity
                setOnClickListener {
                    db.collection("semester")
                        .get().addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot.documents) {
                                val semester = document.toObject<Semester>()
                                val semAbbr = semester?.STag
                                if (semAbbr == semName) {
                                    val intent = Intent(this@BookDetailsActivity, AllBookActivity::class.java)
                                    intent.putExtra("fieldName", "xSem")
                                    intent.putExtra("departmentAbbr", semName)
                                    intent.putExtra("abbr", semester.SAbbr)
                                    intent.putExtra("no", semester.SSem)
                                    intent.putExtra("semesterNo", semester.SSem)
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
                        binding.viewCountTextView.text = "Total Reads: $newViewCount"
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

    private fun displayPointsDynamically() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Convert snapshot to User object
                val user = snapshot.getValue(User::class.java)
                binding.tvPremium.text = user?.points.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@BookDetailsActivity,
                    "Failed to load points",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        displayPointsDynamically()
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

    private fun isShimmerOn(flag: Boolean) {
        if (flag) {
            binding.apply {
                shimmerTvBookTitle.visibility = View.VISIBLE
                shimmerTvBookTitle.startShimmer()
                shimmerBookCoverImage.visibility = View.VISIBLE
                shimmerBookCoverImage.startShimmer()
                shimmerIcBook.visibility = View.VISIBLE
                shimmerIcBook.startShimmer()
                shimmerIcView.visibility = View.VISIBLE
                shimmerIcView.startShimmer()
                shimmerIcCalander.visibility = View.VISIBLE
                shimmerIcCalander.startShimmer()

                icBook.visibility = View.INVISIBLE
                icView.visibility = View.INVISIBLE
                icCalander.visibility = View.INVISIBLE

                shimmerBookCodes.visibility = View.VISIBLE
                shimmerBookCodes.startShimmer()
                shimmerViewCountTextView.visibility = View.VISIBLE
                shimmerViewCountTextView.startShimmer()
                shimmerDataUploaded.visibility = View.VISIBLE
                shimmerDataUploaded.startShimmer()

                bookCodes.visibility = View.INVISIBLE
                viewCountTextView.visibility = View.INVISIBLE
                dataUploaded.visibility = View.INVISIBLE
                bookCoverImage.visibility = View.INVISIBLE
                tvBookTitle.visibility = View.INVISIBLE

                shimmerUnitListContainer.visibility = View.VISIBLE
                shimmerUnitList.visibility = View.VISIBLE
                shimmerUnitList.startShimmer()
                clUnitList.visibility = View.INVISIBLE
            }
        } else {
            binding.apply {
                shimmerTvBookTitle.visibility = View.INVISIBLE
                shimmerTvBookTitle.stopShimmer()
                shimmerBookCoverImage.visibility = View.INVISIBLE
                shimmerBookCoverImage.stopShimmer()
                shimmerIcBook.visibility = View.INVISIBLE
                shimmerIcBook.stopShimmer()
                shimmerIcView.visibility = View.INVISIBLE
                shimmerIcView.stopShimmer()
                shimmerIcCalander.visibility = View.INVISIBLE
                shimmerIcCalander.stopShimmer()

                icBook.visibility = View.VISIBLE
                icView.visibility = View.VISIBLE
                icCalander.visibility = View.VISIBLE

                shimmerBookCodes.visibility = View.INVISIBLE
                shimmerBookCodes.stopShimmer()
                shimmerViewCountTextView.visibility = View.INVISIBLE
                shimmerViewCountTextView.stopShimmer()
                shimmerDataUploaded.visibility = View.INVISIBLE
                shimmerDataUploaded.stopShimmer()

                bookCodes.visibility = View.VISIBLE
                viewCountTextView.visibility = View.VISIBLE
                dataUploaded.visibility = View.VISIBLE
                bookCoverImage.visibility = View.VISIBLE
                tvBookTitle.visibility = View.VISIBLE

                shimmerUnitListContainer.visibility = View.INVISIBLE
                shimmerUnitList.visibility = View.INVISIBLE
                shimmerUnitList.stopShimmer()
                clUnitList.visibility = View.VISIBLE
            }
        }
    }
}