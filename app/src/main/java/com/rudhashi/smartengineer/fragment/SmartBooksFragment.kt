package com.rudhashi.smartengineer.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.BookGridAdapter
import com.rudhashi.smartengineer.adapter.BookListAdapter
import com.rudhashi.smartengineer.databinding.FragmentSmartBooksBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.SmartData.Companion.setHeightBasedOnItems
import com.rudhashi.smartengineer.view.BookDetailsActivity
import com.rudhashi.smartengineer.view.ProfileActivity

class SmartBooksFragment : Fragment() {

    private lateinit var binding: FragmentSmartBooksBinding
    private lateinit var bookGridAdapter: BookGridAdapter
    private lateinit var bookListAdapter: BookListAdapter
    private var db: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmartBooksBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        // eBook Open and Redeem Points
        bookGridAdapter = BookGridAdapter { bookCode ->
            redeemPoints(bookCode)
        }

        binding.apply {
            btnViewProfile.setOnClickListener { startActivity(Intent(requireActivity(), ProfileActivity::class.java)) }
            btnViewPremium.setOnClickListener { SmartData.comingSoon(requireActivity()) }

            searchBox.setOnClickListener { SmartData.comingSoon(requireActivity()) }

            fetchBooks("TimeStamp", false)
            btnGrid.setBackgroundResource(R.color.transparent)
            btnList.setBackgroundResource(R.drawable.sl_search_box_icon)

            btnList.setOnClickListener {
                btnList.setBackgroundResource(R.drawable.sl_search_box_icon)
                btnGrid.setBackgroundResource(R.color.transparent)
                fetchBooks("TimeStamp", false)
            }
            btnGrid.setOnClickListener {
                btnList.setBackgroundResource(R.color.transparent)
                btnGrid.setBackgroundResource(R.drawable.sl_search_box_icon)
                fetchBooks("TimeStamp", true)
            }
            btnLatest.setOnClickListener {
                fetchBooks("TimeStamp", false)
                btnLatest.setBackgroundResource(R.drawable.sl_search_box_icon)
                btnPopular.setBackgroundResource(R.color.transparent)
                btnGrid.setBackgroundResource(R.color.transparent)
                btnList.setBackgroundResource(R.drawable.sl_search_box_icon)

                btnList.setOnClickListener {
                    btnList.setBackgroundResource(R.drawable.sl_search_box_icon)
                    btnGrid.setBackgroundResource(R.color.transparent)
                    fetchBooks("TimeStamp", false)
                }
                btnGrid.setOnClickListener {
                    btnList.setBackgroundResource(R.color.transparent)
                    btnGrid.setBackgroundResource(R.drawable.sl_search_box_icon)
                    fetchBooks("TimeStamp", true)
                }
            }
            btnPopular.setOnClickListener {
                fetchBooks("BView", true)
                btnLatest.setBackgroundResource(R.color.transparent)
                btnPopular.setBackgroundResource(R.drawable.sl_search_box_icon)
                btnList.setBackgroundResource(R.color.transparent)
                btnGrid.setBackgroundResource(R.drawable.sl_search_box_icon)

                btnList.setOnClickListener {
                    btnList.setBackgroundResource(R.drawable.sl_search_box_icon)
                    btnGrid.setBackgroundResource(R.color.transparent)
                    fetchBooks("BView", false)
                }
                btnGrid.setOnClickListener {
                    btnList.setBackgroundResource(R.color.transparent)
                    btnGrid.setBackgroundResource(R.drawable.sl_search_box_icon)
                    fetchBooks("BView", true)
                }
            }
        }
        return binding.root
    }

    // =====================================================================================================================
    // =======================================       Get Book Data                  ========================================
    // =====================================================================================================================
    private fun fetchBooks(sortBy: String, isGrid: Boolean) {
        if (isGrid){
            binding.rvBook.layoutManager = GridLayoutManager(requireActivity(),2)
            binding.rvBook.setHasFixedSize(true)
            db?.collection("books")?.orderBy(sortBy, Query.Direction.DESCENDING) // Or DESCENDING for reverse order
                ?.get()?.addOnSuccessListener { querySnapshot ->
                    val books = querySnapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                    bookGridAdapter.setData(books)
                }?.addOnFailureListener { e ->
                    Toast.makeText(requireActivity(), "Failed to fetch books: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            binding.rvBook.adapter = bookGridAdapter
        } else {
            binding.rvBook.layoutManager = LinearLayoutManager(requireActivity())

            // eBook Open and Redeem Points
            bookListAdapter = BookListAdapter { bookCode ->
                redeemPoints(bookCode)
            }

            //redeemPoints("List")

            val layoutParams = binding.rvBook.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            binding.rvBook.layoutParams = layoutParams

            binding.rvBook.setHasFixedSize(true)
            binding.rvBook.adapter = bookListAdapter
            db?.collection("books")?.orderBy(sortBy, Query.Direction.DESCENDING) // Or DESCENDING for reverse order
                ?.get()?.addOnSuccessListener { querySnapshot ->
                    val books = querySnapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                    bookListAdapter.updateBooks(books)
                }?.addOnFailureListener { e ->
                    Toast.makeText(requireActivity(), "Failed to fetch books: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
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

                val intent = Intent(requireContext(), BookDetailsActivity::class.java)
                userRef.setValue(newPoints)
                intent.putExtra("BOOK_CODE", bookCode)
                startActivity(intent)
            } else {
                Toast.makeText(requireActivity(), "Not enough points!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}