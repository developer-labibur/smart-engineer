package com.softylasa.smartengineer.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowMetrics
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdSize
import com.google.firebase.firestore.FirebaseFirestore
import com.softylasa.smartengineer.data.models.Book
import com.softylasa.smartengineer.databinding.ActivitySearchBinding
import com.softylasa.smartengineer.ui.adapters.BookAdapter
import com.softylasa.smartengineer.managers.AdManager
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity

class SearchActivity : SmartThemeActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: BookAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge() // Ensure UI uses edge-to-edge design.

        // Apply window insets for immersive design.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BookAdapter(mutableListOf()) { book ->
            // Handle book item click
            handleBookClick(book)
        }
        binding.recyclerView.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        // Listen to text changes in the search box
        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(query: Editable?) {
                val searchQuery = query.toString()
                if (searchQuery.isNotEmpty()) {
                    searchBooks(searchQuery)
                } else {
                    adapter.updateBooks(emptyList()) // Clear RecyclerView when query is empty
                }
            }
        })

        AdManager.setBannerAds(this, binding.adViewContainer, adSize, true)

    }


    // Perform Firebase search
    private fun searchBooks(query: String) {
        Log.d("SearchActivity", "Searching for: $query")

        val books = mutableListOf<Book>()

        db.collection("books")
            .whereGreaterThanOrEqualTo("BName", query)
            .whereLessThanOrEqualTo("BName", query + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                Log.d("SearchActivity", "Found ${result.size()} results for BName")
                for (document in result.documents) {
                    val book = document.toObject(Book::class.java)
                    if (book != null) books.add(book)
                }

                // Check books after first query
                Log.d("SearchActivity", "Books after BName query: $books")

                // Add more queries for BCode
                db.collection("books")
                    .whereGreaterThanOrEqualTo("BCode", query)
                    .whereLessThanOrEqualTo("BCode", query + "\uf8ff")
                    .get()
                    .addOnSuccessListener { codeResult ->
                        Log.d("SearchActivity", "Found ${codeResult.size()} results for BCode")
                        for (document in codeResult.documents) {
                            val book = document.toObject(Book::class.java)
                            if (book != null && !books.contains(book)) books.add(book)
                        }

                        // Update adapter with results
                        Log.d("SearchActivity", "Final books list: $books")
                        adapter.updateBooks(books)
                    }
                    .addOnFailureListener { e ->
                        Log.e("SearchActivity", "Error fetching BCode results", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("SearchActivity", "Error fetching BName results", e)
            }
    }


    // Handle book click event
    private fun handleBookClick(book: Book) {
        // Navigate to book details or perform an action
        val intent = Intent(this, BookDetailsActivity::class.java)
        intent.putExtra("BOOK_CODE", book.BCode)
        startActivity(intent)
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

    // Enable edge-to-edge layout
    @Suppress("DEPRECATION")
    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
}