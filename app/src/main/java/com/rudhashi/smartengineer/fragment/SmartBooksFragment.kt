package com.rudhashi.smartengineer.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.BookGridAdapter
import com.rudhashi.smartengineer.adapter.BookListAdapter
import com.rudhashi.smartengineer.databinding.FragmentSmartBooksBinding
import com.rudhashi.smartengineer.manager.PointManager
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.view.ProfileActivity
import com.rudhashi.smartengineer.view.SmartPointActivity
import com.rudhashi.smartengineer.view.SmartViewActivity

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
            PointManager.redeemPointsOpenEBook(requireActivity(), bookCode)
        }

        binding.apply {
            btnViewProfile.setOnClickListener { startActivity(Intent(requireActivity(), ProfileActivity::class.java)) }
            btnViewPremium.setOnClickListener { startActivity(Intent(requireActivity(), SmartPointActivity::class.java)) }

            searchBox.setOnClickListener { SmartData.comingSoon(requireActivity()) }

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
            btnViewAll.setOnClickListener {
                val intent = Intent(requireContext(), SmartViewActivity::class.java)
                intent.putExtra("DISPLAY", "Book")
                startActivity(intent)
            }
        }
        return binding.root
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
                PointManager.redeemPointsOpenEBook(requireActivity(), bookCode)
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

}