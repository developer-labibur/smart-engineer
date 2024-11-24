package com.rudhashi.smartengineer.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rudhashi.smartengineer.adapter.BookAdapter
import com.rudhashi.smartengineer.databinding.ActivityAllBookBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.util.SmartData

class AllBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllBookBinding
    private lateinit var bookAdapter: BookAdapter
    private lateinit var query: Query
    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllBookBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setStatusBarFontColor()

        // Retrieve data from Intent
        val fieldName = intent.getStringExtra("fieldName")
        val semesterCode = intent.getStringExtra("semesterCode")
        val departmentName = intent.getStringExtra("departmentName")
        val semesterName = intent.getStringExtra("semesterName")
        binding.tvHeaderTitle.text = semesterName
        binding.departmentName.text = departmentName

        bookAdapter = BookAdapter { bookCode ->
            val intent = Intent(this, BookDetailsActivity::class.java)
            intent.putExtra("BOOK_CODE", bookCode)
            startActivity(intent)
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
    private fun setStatusBarFontColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance( 0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }
}