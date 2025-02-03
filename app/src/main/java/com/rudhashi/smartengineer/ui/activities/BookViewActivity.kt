package com.rudhashi.smartengineer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.rudhashi.smartengineer.utils.themes.SmartThemeActivity
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.data.models.Book
import com.rudhashi.smartengineer.data.models.Favorite
import com.rudhashi.smartengineer.databinding.ActivityBookViewBinding
import com.rudhashi.smartengineer.ui.adapters.BookGridAdapter
import com.rudhashi.smartengineer.ui.adapters.BookListAdapter
import com.rudhashi.smartengineer.utils.helpers.Converters.covertDepartmentToAbbr
import com.rudhashi.smartengineer.utils.helpers.Converters.covertSemNameToCode

class BookViewActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityBookViewBinding

    private val dbRealtime = FirebaseDatabase.getInstance().reference
    private val dbFirestore = FirebaseFirestore.getInstance()

    private val favoriteBooks = mutableListOf<Book>()
    private val booksList = mutableListOf<Book>()

    private lateinit var booksGridAdapter: BookGridAdapter
    private lateinit var bookListAdapter: BookListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookViewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Handle insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // eBook Open and Redeem Points
        booksGridAdapter = BookGridAdapter(){ bookCode ->
            val intent = Intent(this, BookDetailsActivity::class.java)
            intent.putExtra("BOOK_CODE", bookCode)
            startActivity(intent)
        }

        setupRecyclerView() // Setup the viewPager

        binding.btnBack.setOnClickListener { finish() }

    }

    // =====================================================================================================================
    // ========================     Setup Bottom Navigation and Main Container (ViewPager)      ============================
    // =====================================================================================================================
    private fun setupRecyclerView() {
        binding.apply {

            btnList.setOnClickListener {
                btnList.setBackgroundResource(R.drawable.smart_view_active_bg)
                btnGrid.setBackgroundResource(R.color.transparent)
                fetchBooks("Library", false)
                isShimmerOn(true, isGrid = false)
            }
            btnGrid.setOnClickListener {
                btnList.setBackgroundResource(R.color.transparent)
                btnGrid.setBackgroundResource(R.drawable.smart_view_active_bg)
                fetchBooks("Library", true)
                isShimmerOn(true, isGrid = true)
            }
            btnLibrary.setOnClickListener { setActiveTab("Library") }
            btnFavourite.setOnClickListener { setActiveTab("Favourite") }
        }
    }

    // =====================================================================================================================
    // =======================================       Get Book Data                  ========================================
    // =====================================================================================================================
    private fun fetchBooks(viewType: String, isGrid: Boolean) {
        if (viewType == "Library"){
            if (isGrid){
                binding.rvBook.layoutManager = GridLayoutManager(this,2)

                val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserUID != null) {
                    val userRef = dbFirestore.collection("users").document(currentUserUID)
                    userRef.get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val department = documentSnapshot.getString("department")
                            val semester = documentSnapshot.getString("semester")

                            if (!department.isNullOrEmpty() && !semester.isNullOrEmpty()) {
                                // Convert department and semester to codes
                                val departmentAbbr = covertDepartmentToAbbr(department)
                                val semesterCode = covertSemNameToCode(semester)
                                val xSem = "$departmentAbbr$semesterCode"

                                val booksRef = dbFirestore.collection("books")
                                booksRef.whereArrayContains("xSem", xSem).get()
                                    .addOnSuccessListener { querySnapshot ->
                                        isShimmerOn(false, isGrid = true)
                                        booksList.clear() // Clear previous data
                                        for (document in querySnapshot) {
                                            val book = document.toObject(Book::class.java)
                                            booksList.add(book)
                                        }
                                        // Update RecyclerView with the fetched books
                                        booksGridAdapter.setData(booksList, false)
                                    }
                                    .addOnFailureListener { exception ->
                                        isShimmerOn(false, isGrid = true)
                                        Log.e("FirestoreError", "Error fetching books: ${exception.message}")
                                    }
                                binding.rvBook.adapter = booksGridAdapter
                            } else {

                                Log.e("UserDataError", "Department or Semester is missing for the user.")
                            }
                        } else {
                            Log.e("FirestoreError", "User document does not exist.")
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("FirestoreError", "Error fetching user data: ${exception.message}")
                    }
                }
            } else {
                binding.rvBook.layoutManager = LinearLayoutManager(this)

                // eBook Open and Redeem Points
                bookListAdapter = BookListAdapter { bookCode ->
                    val intent = Intent(this, BookDetailsActivity::class.java)
                    intent.putExtra("BOOK_CODE", bookCode)
                    startActivity(intent)
                }

                val layoutParams = binding.rvBook.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                binding.rvBook.layoutParams = layoutParams

                binding.rvBook.adapter = bookListAdapter

                val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserUID != null) {
                    val userRef = dbFirestore.collection("users").document(currentUserUID)
                    userRef.get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val department = documentSnapshot.getString("department")
                            val semester = documentSnapshot.getString("semester")

                            if (!department.isNullOrEmpty() && !semester.isNullOrEmpty()) {
                                // Convert department and semester to codes
                                val departmentAbbr = covertDepartmentToAbbr(department)
                                val semesterCode = covertSemNameToCode(semester)
                                val xSem = "$departmentAbbr$semesterCode"

                                val booksRef = dbFirestore.collection("books")
                                booksRef.whereArrayContains("xSem", xSem).get()
                                    .addOnSuccessListener { querySnapshot ->
                                        isShimmerOn(false, isGrid = false)
                                        booksList.clear() // Clear previous data
                                        for (document in querySnapshot) {
                                            val book = document.toObject(Book::class.java)
                                            booksList.add(book)
                                        }
                                        // Update RecyclerView with the fetched books
                                        bookListAdapter.updateBooks(booksList, false)
                                    }
                                    .addOnFailureListener { exception ->
                                        isShimmerOn(false, isGrid = false)
                                        Log.e("FirestoreError", "Error fetching books: ${exception.message}")
                                    }
                                binding.rvBook.adapter = bookListAdapter
                            } else {

                                Log.e("UserDataError", "Department or Semester is missing for the user.")
                            }
                        } else {
                            Log.e("FirestoreError", "User document does not exist.")
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("FirestoreError", "Error fetching user data: ${exception.message}")
                    }
                }
            }
        } else {
            if (isGrid){
                binding.rvBook.layoutManager = GridLayoutManager(this,2)

                val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserUID != null) {
                    val favoritesRef = dbRealtime.child("UsersDB").child(currentUserUID).child("Favourites").child("Books")
                    favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                favoriteBooks.clear() // Clear previous data
                                // Iterate through all the BCode in the favorites list
                                for (favoriteSnapshot in snapshot.children) {
                                    val favorite = favoriteSnapshot.getValue(Favorite::class.java)

                                    val bookRef = favorite?.let { dbFirestore.collection("books").document(it.BCode) }
                                    bookRef?.get()?.addOnSuccessListener { documentSnapshot ->
                                        isShimmerOn(false, isGrid = true)
                                        if (documentSnapshot.exists()) {
                                            val book = documentSnapshot.toObject(Book::class.java)
                                            book?.let {
                                                // Add book to the favoriteBooks list
                                                favoriteBooks.add(it)

                                                // Once all books are fetched, update the RecyclerView
                                                if (this@BookViewActivity.favoriteBooks.size == favoriteBooks.size) {
                                                    booksGridAdapter.setData(favoriteBooks, false) // Pass your favoriteBooks list to the adapter
                                                }
                                            }
                                        }
                                    }?.addOnFailureListener { exception ->
                                        isShimmerOn(false, isGrid = true)
                                        Log.e("FirebaseError", "Error fetching book details: ${exception.message}")
                                    }
                                    binding.rvBook.adapter = booksGridAdapter
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle errors if any
                            Log.e("FirebaseError", "Error fetching favorites: ${error.message}")
                        }
                    })
                }
            } else {
                binding.rvBook.layoutManager = LinearLayoutManager(this)

                // eBook Open and Redeem Points
                bookListAdapter = BookListAdapter { bookCode ->
                    //PointManager.redeemPointsOpenEBook(requireActivity(), bookCode)
                }

                val layoutParams = binding.rvBook.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                binding.rvBook.layoutParams = layoutParams

                binding.rvBook.adapter = bookListAdapter

                val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserUID != null) {
                    val favoritesRef = dbRealtime.child("UsersDB").child(currentUserUID).child("Favourites").child("Books")
                    favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                favoriteBooks.clear() // Clear previous data
                                // Iterate through all the BCode in the favorites list
                                for (favoriteSnapshot in snapshot.children) {
                                    val favorite = favoriteSnapshot.getValue(Favorite::class.java)

                                    val bookRef = favorite?.let { dbFirestore.collection("books").document(it.BCode) }
                                    bookRef?.get()?.addOnSuccessListener { documentSnapshot ->
                                        isShimmerOn(false, isGrid = false)
                                        if (documentSnapshot.exists()) {
                                            val book = documentSnapshot.toObject(Book::class.java)
                                            book?.let {
                                                // Add book to the favoriteBooks list
                                                favoriteBooks.add(it)

                                                // Once all books are fetched, update the RecyclerView
                                                if (this@BookViewActivity.favoriteBooks.size == favoriteBooks.size) {
                                                    bookListAdapter.updateBooks(favoriteBooks, false) // Pass your favoriteBooks list to the adapter
                                                }
                                            }
                                        }
                                    }?.addOnFailureListener { exception ->
                                        isShimmerOn(false, isGrid = false)
                                        Log.e("FirebaseError", "Error fetching book details: ${exception.message}")
                                    }
                                    binding.rvBook.adapter = bookListAdapter
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle errors if any
                            Log.e("FirebaseError", "Error fetching favorites: ${error.message}")
                        }
                    })
                }
            }
        }

    }

    // =====================================================================================================================
    // ===================================       Get Library Book Data                  ====================================
    // =====================================================================================================================
    // Fetch user's department and semester to generate `xSem`, then fetch books


    override fun onResume() {
        super.onResume()

        val displayFragment = intent.getStringExtra("DISPLAY").toString()
        when (displayFragment) {
            "Library" -> setActiveTab("Library")
            "Favourite" -> setActiveTab("Favourite")
        }

    }

    private fun setActiveTab(tNo: String){
        if (tNo == "Library"){
            binding.apply {
                fetchBooks("Library", false)
                isShimmerOn(true, isGrid = false)
                btnLibrary.setBackgroundResource(R.drawable.smart_view_active_bg)
                btnFavourite.setBackgroundResource(R.color.transparent)
                btnGrid.setBackgroundResource(R.color.transparent)
                btnList.setBackgroundResource(R.drawable.smart_view_active_bg)

                btnList.setOnClickListener {
                    isShimmerOn(true, isGrid = false)
                    btnList.setBackgroundResource(R.drawable.smart_view_active_bg)
                    btnGrid.setBackgroundResource(R.color.transparent)
                    fetchBooks("Library", false)
                }
                btnGrid.setOnClickListener {
                    btnList.setBackgroundResource(R.color.transparent)
                    btnGrid.setBackgroundResource(R.drawable.smart_view_active_bg)
                    fetchBooks("Library", true)
                    isShimmerOn(true, isGrid = true)
                }
            }
        }
        else if (tNo == "Favourite") {
            binding.apply {
                fetchBooks("Favourite", true)
                isShimmerOn(true, isGrid = true)
                btnLibrary.setBackgroundResource(R.color.transparent)
                btnFavourite.setBackgroundResource(R.drawable.smart_view_active_bg)
                btnList.setBackgroundResource(R.color.transparent)
                btnGrid.setBackgroundResource(R.drawable.smart_view_active_bg)

                btnList.setOnClickListener {
                    btnList.setBackgroundResource(R.drawable.smart_view_active_bg)
                    btnGrid.setBackgroundResource(R.color.transparent)
                    fetchBooks("Favourite", false)
                    isShimmerOn(true, isGrid = false)
                }
                btnGrid.setOnClickListener {
                    btnList.setBackgroundResource(R.color.transparent)
                    btnGrid.setBackgroundResource(R.drawable.smart_view_active_bg)
                    fetchBooks("Favourite", true)
                    isShimmerOn(true, isGrid = true)
                }
            }
        } else{
            binding.apply {
                btnGrid.setBackgroundResource(R.color.transparent)
                btnList.setBackgroundResource(R.drawable.smart_view_active_bg)
                fetchBooks("Library", true)
                isShimmerOn(true, isGrid = true)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isShimmerOn(false, isGrid = false)
    }

    @Suppress("SameParameterValue")
    private fun isShimmerOn(flag: Boolean, isGrid: Boolean) {
        binding.apply {
            if (flag) {
                if (isGrid) {
                    shimmerGrid.startShimmer()
                    shimmerGrid.visibility = View.VISIBLE
                } else {
                    shimmerList.startShimmer()
                    shimmerList.visibility = View.VISIBLE
                }
                rvBook.visibility = View.GONE
            } else {
                if (isGrid) {
                    shimmerGrid.stopShimmer()
                    shimmerGrid.visibility = View.GONE
                } else {
                    shimmerList.stopShimmer()
                    shimmerList.visibility = View.GONE
                }
                rvBook.visibility = View.VISIBLE
            }
        }
    }
}