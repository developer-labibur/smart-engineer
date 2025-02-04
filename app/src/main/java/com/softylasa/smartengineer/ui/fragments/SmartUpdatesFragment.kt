package com.softylasa.smartengineer.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.models.Book
import com.softylasa.smartengineer.ui.adapters.NoticeAdapter
import com.softylasa.smartengineer.databinding.FragmentSmartUpdatesBinding
import com.softylasa.smartengineer.data.models.Notice
import com.softylasa.smartengineer.managers.DialogManager.showFreemiumFeaturesDialog
import com.softylasa.smartengineer.network.NoticeResponse
import com.softylasa.smartengineer.network.RetrofitClient
import com.softylasa.smartengineer.ui.activities.AcademicBookActivity
import com.softylasa.smartengineer.ui.activities.BookDetailsActivity
import com.softylasa.smartengineer.ui.activities.NoticeBoardActivity
import com.softylasa.smartengineer.utils.helpers.SmartData
import com.softylasa.smartengineer.ui.activities.ProfileActivity
import com.softylasa.smartengineer.ui.activities.SmartPointActivity
import com.softylasa.smartengineer.ui.activities.SmartViewActivity
import com.softylasa.smartengineer.ui.adapters.BookGridAdapter
import com.softylasa.smartengineer.ui.adapters.BookListAdapter
import com.softylasa.smartengineer.ui.adapters.BtebNoticeAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SmartUpdatesFragment : Fragment() {

    private lateinit var binding: FragmentSmartUpdatesBinding

    private lateinit var bookGridAdapter: BookGridAdapter
    private lateinit var bookListAdapter: BookListAdapter
    private var db: FirebaseFirestore? = null

    // RecyclerView Adapter
    private lateinit var btebNoticeAdapter: BtebNoticeAdapter // Replace with your RecyclerView adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmartUpdatesBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        bookGridAdapter = BookGridAdapter { bookCode ->
            val intent = Intent(activity, BookDetailsActivity::class.java)
            intent.putExtra("BOOK_CODE", bookCode)
            startActivity(intent)
        }

        btebNoticeAdapter = BtebNoticeAdapter(emptyList(), requireActivity())

        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        binding.rvNotice.adapter = btebNoticeAdapter
        binding.rvNotice.layoutManager = LinearLayoutManager(requireActivity())

        // Fetch notices from Firestore, ordered by timestamp in descending order
        fetchNotices()
        isShimmerNoticeOn(true)

        binding.apply {
            btnViewProfile.setOnClickListener { startActivity(Intent(requireActivity(), ProfileActivity::class.java)) }
            btnViewPremium.setOnClickListener { showFreemiumFeaturesDialog(requireActivity()) }

            btnNotice.setBackgroundResource(R.drawable.sl_search_box_icon)
            btnBooks.setBackgroundResource(R.color.transparent)

            containerNotice.visibility = View.VISIBLE
            containerBook.visibility = View.GONE

            btnNotice.setOnClickListener {
                btnNotice.setBackgroundResource(R.drawable.sl_search_box_icon)
                btnBooks.setBackgroundResource(R.color.transparent)

                containerNotice.visibility = View.VISIBLE
                containerBook.visibility = View.GONE

                fetchNotices()
                isShimmerNoticeOn(true)

                btnViewAllNotice.visibility = View.GONE
                btnViewAllNotice.setOnClickListener {
                    val intent = Intent(requireContext(), NoticeBoardActivity::class.java)
                    startActivity(intent)
                }
            }


            btnBooks.setOnClickListener {
                btnNotice.setBackgroundResource(R.color.transparent)
                btnBooks.setBackgroundResource(R.drawable.sl_search_box_icon)

                containerNotice.visibility = View.GONE
                containerBook.visibility = View.VISIBLE

                fetchBooks("BUpdates", false)
                isShimmerOn(true, isGrid = false)
                btnGrid.setBackgroundResource(R.color.transparent)
                btnList.setBackgroundResource(R.drawable.sl_search_box_icon)

                btnList.setOnClickListener {
                    btnList.setBackgroundResource(R.drawable.sl_search_box_icon)
                    btnGrid.setBackgroundResource(R.color.transparent)
                    fetchBooks("BUpdates", false)
                    isShimmerOn(true, isGrid = false)
                }
                btnGrid.setOnClickListener {
                    btnList.setBackgroundResource(R.color.transparent)
                    btnGrid.setBackgroundResource(R.drawable.sl_search_box_icon)
                    fetchBooks("BUpdates", true)
                    isShimmerOn(true, isGrid = true)
                }
                btnLatest.setOnClickListener {
                    fetchBooks("BUpdates", false)
                    isShimmerOn(true, isGrid = false)
                    btnLatest.setBackgroundResource(R.drawable.sl_search_box_icon)
                    btnPopular.setBackgroundResource(R.color.transparent)
                    btnGrid.setBackgroundResource(R.color.transparent)
                    btnList.setBackgroundResource(R.drawable.sl_search_box_icon)

                    btnList.setOnClickListener {
                        isShimmerOn(true, isGrid = false)
                        btnList.setBackgroundResource(R.drawable.sl_search_box_icon)
                        btnGrid.setBackgroundResource(R.color.transparent)
                        fetchBooks("BUpdates", false)
                    }
                    btnGrid.setOnClickListener {
                        btnList.setBackgroundResource(R.color.transparent)
                        btnGrid.setBackgroundResource(R.drawable.sl_search_box_icon)
                        fetchBooks("BUpdates", true)
                        isShimmerOn(true, isGrid = true)
                    }
                }
                btnPopular.setOnClickListener {
                    fetchBooks("BView", true)
                    isShimmerOn(true, isGrid = true)
                    btnLatest.setBackgroundResource(R.color.transparent)
                    btnPopular.setBackgroundResource(R.drawable.sl_search_box_icon)
                    btnList.setBackgroundResource(R.color.transparent)
                    btnGrid.setBackgroundResource(R.drawable.sl_search_box_icon)

                    btnList.setOnClickListener {
                        btnList.setBackgroundResource(R.drawable.sl_search_box_icon)
                        btnGrid.setBackgroundResource(R.color.transparent)
                        fetchBooks("BView", false)
                        isShimmerOn(true, isGrid = false)
                    }
                    btnGrid.setOnClickListener {
                        btnList.setBackgroundResource(R.color.transparent)
                        btnGrid.setBackgroundResource(R.drawable.sl_search_box_icon)
                        fetchBooks("BView", true)
                        isShimmerOn(true, isGrid = true)
                    }
                }
                btnViewAll.visibility = View.INVISIBLE
                btnViewAll.setOnClickListener { startActivity(Intent(requireContext(), AcademicBookActivity::class.java)) }

            }

            btnViewAllNotice.visibility = View.GONE
            btnViewAllNotice.setOnClickListener {
                val intent = Intent(requireContext(), NoticeBoardActivity::class.java)
                startActivity(intent)
            }
        }

        return binding.root
    }

    private fun fetchNotices() {
        // Retrofit network call
        RetrofitClient.apiService.getNotices().enqueue(object : Callback<NoticeResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<NoticeResponse>, response: Response<NoticeResponse>) {
                if (response.isSuccessful) {
                    isShimmerNoticeOn(false)
                    btebNoticeAdapter.updateNoticeList(response.body()?.notices ?: emptyList(), true) // Update the adapter
                    binding.btnViewAllNotice.visibility = View.VISIBLE
                } else {
                    //showError("Failed to load notices. Please try again.")
                    isShimmerNoticeOn(false)
                }
                btebNoticeAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<NoticeResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Unknown error")
                //showError("Failed to load notices. Please check your connection and try again.")
                isShimmerNoticeOn(false)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        isShimmerNoticeOn(false)
        isShimmerOn(false, isGrid = false)
    }

    private fun isShimmerNoticeOn(flag: Boolean){
        binding.apply {
            if (flag){
                shimmerNotice.startShimmer()
                shimmerNotice.visibility = View.VISIBLE
                updateContainer.visibility = View.GONE
            } else {
                shimmerNotice.stopShimmer()
                shimmerNotice.visibility = View.GONE
                updateContainer.visibility = View.VISIBLE
            }
        }
    }

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
                bookContainer.visibility = View.GONE
            } else {
                if (isGrid) {
                    shimmerGrid.stopShimmer()
                    shimmerGrid.visibility = View.GONE
                } else {
                    shimmerList.stopShimmer()
                    shimmerList.visibility = View.GONE
                }
                bookContainer.visibility = View.VISIBLE
            }
        }
    }

    // =====================================================================================================================
    // =======================================       Get Book Data                  ========================================
    // =====================================================================================================================
    @Suppress("KotlinConstantConditions")
    private fun fetchBooks(sortBy: String, isGrid: Boolean) {
        if (isGrid){
            binding.rvBook.layoutManager = GridLayoutManager(requireActivity(),2)
            binding.rvBook.setHasFixedSize(true)
            db?.collection("books")
                ?.orderBy(sortBy, Query.Direction.DESCENDING) // Or DESCENDING for reverse order
                ?.get()?.addOnSuccessListener { querySnapshot ->
                    isShimmerOn(false, isGrid = true)
                    val books = querySnapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                    bookGridAdapter.setData(books, true)
                    binding.btnViewAll.visibility = View.VISIBLE
                }?.addOnFailureListener { e ->
                    isShimmerOn(false, isGrid)
                    Toast.makeText(requireActivity(), "Failed to fetch books: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    binding.btnViewAll.visibility = View.VISIBLE
                }
            binding.rvBook.adapter = bookGridAdapter
        } else {
            binding.rvBook.layoutManager = LinearLayoutManager(requireActivity())

            // eBook Open and Redeem Points
            bookListAdapter = BookListAdapter { bookCode ->
                val intent = Intent(activity, BookDetailsActivity::class.java)
                intent.putExtra("BOOK_CODE", bookCode)
                startActivity(intent)
            }

            //redeemPoints("List")

            val layoutParams = binding.rvBook.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            binding.rvBook.layoutParams = layoutParams

            binding.rvBook.setHasFixedSize(true)
            binding.rvBook.adapter = bookListAdapter
            db?.collection("books")?.orderBy(sortBy, Query.Direction.DESCENDING) // Or DESCENDING for reverse order
                ?.get()?.addOnSuccessListener { querySnapshot ->
                    isShimmerOn(false, isGrid = false)
                    val books = querySnapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                    bookListAdapter.updateBooks(books, true)
                    binding.btnViewAll.visibility = View.VISIBLE
                }?.addOnFailureListener { e ->
                    isShimmerOn(false, isGrid)
                    Toast.makeText(requireActivity(), "Failed to fetch books: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    binding.btnViewAll.visibility = View.VISIBLE
                }
        }
    }

}