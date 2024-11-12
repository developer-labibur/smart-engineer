package com.rudhashi.smartengineer.fragment

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.BookGridAdapter
import com.rudhashi.smartengineer.adapter.CarouselAdapter
import com.rudhashi.smartengineer.adapter.CategoryAdapter
import com.rudhashi.smartengineer.adapter.NoticeQuoteAdapter
import com.rudhashi.smartengineer.databinding.FragmentHomeBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.model.CategoryDataClass
import com.rudhashi.smartengineer.model.Notice
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.view.MainActivity

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var tagLog = "HomeFragment"

    // Variable for String Array ---------------------------------------------------------------------------------
    private lateinit var categoryDataList: ArrayList<CategoryDataClass>
    private lateinit var catImageList: Array<String>
    private lateinit var catTitleList: Array<String>
    private lateinit var catIdList: Array<String>

    // Book Grid Code --------------------------------------------------------------------------------
    private lateinit var bookGridAdapter: BookGridAdapter

    private lateinit var noticeQuoteAdapter: NoticeQuoteAdapter // Replace with your RecyclerView adapter
    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        noticeQuoteAdapter = NoticeQuoteAdapter(emptyList(), requireActivity())

        // Initialize category data list
        categoryDataList = ArrayList()

        // Initialize String Array for category image, title, and id
        catImageList = resources.getStringArray(R.array.arrayCategoryImageUrl)
        catTitleList = resources.getStringArray(R.array.arrayCategoryTitle)
        catIdList = resources.getStringArray(R.array.arrayCategoryId)

        // Gradient Text color Set -----------------------------------------------------------------------------------------
        binding.tvSmart.paint.shader = LinearGradient(
            0f,
            0f,
            binding.tvSmart.paint.measureText(binding.tvSmart.text.toString()),
            binding.tvSmart.textSize,
            intArrayOf(
                resources.getColor(R.color.gradient_start, null),
                resources.getColor(R.color.gradient_start, null),
                resources.getColor(R.color.gradient_end, null)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        binding.tvE.paint.shader = LinearGradient(
            0f,
            0f,
            binding.tvE.paint.measureText(binding.tvE.text.toString()),
            binding.tvE.textSize,
            intArrayOf(
                resources.getColor(R.color.clr2_1, null),
                resources.getColor(R.color.clr1_1, null),
                resources.getColor(R.color.clr2_1, null)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        binding.tvEngineer.paint.shader = LinearGradient(
            0f,
            0f,
            binding.tvEngineer.paint.measureText(binding.tvEngineer.text.toString()),
            binding.tvEngineer.textSize,
            intArrayOf(
                resources.getColor(R.color.clr2_1_1, null),
                resources.getColor(R.color.clr2_1_2, null),
                resources.getColor(R.color.clr2_1_3, null)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        binding.tvPoly.paint.shader = LinearGradient(
            0f,
            0f,
            binding.tvPoly.paint.measureText(binding.tvPoly.text.toString()),
            binding.tvPoly.textSize,
            intArrayOf(
                resources.getColor(R.color.clr2_1_2, null),
                resources.getColor(R.color.clr2_1_1, null),
                resources.getColor(R.color.clr2_1_1, null)
            ),
            null,
            Shader.TileMode.CLAMP
        )

        // Adapter RecyclerView Category -----------------------------------------------------------------------------------
        binding.recycleCategory.layoutManager =
            GridLayoutManager(requireActivity(), 3, GridLayoutManager.VERTICAL, false)
        binding.recycleCategory.setHasFixedSize(true)
        binding.recycleCategory.isNestedScrollingEnabled = false;
        //todo: catAdapter.notifyDataSetChanged(); // Extra
        categoryDataList = arrayListOf<CategoryDataClass>()
        getCategoryData()

        setUpCarouselRecyclerView()


        bookGridAdapter = BookGridAdapter { bookCode ->
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.putExtra("BOOK_CODE", bookCode)
            startActivity(intent)
        }

        binding.apply {
            recycleBooks.layoutManager = GridLayoutManager(requireActivity(),1, GridLayoutManager.HORIZONTAL,false)
            recycleBooks.adapter = bookGridAdapter

            btnMyLibrary.setOnClickListener { fetchBooks() }
            btnMyFavorites.setOnClickListener { fetchBooks() }
        }

        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        binding.rv.adapter = noticeQuoteAdapter
        binding.rv.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

        // Fetch notices from Firestore, ordered by timestamp in descending order
        fetchNotices()

        return binding.root
    }

    // =====================================================================================================================
    // ============================       Get Category and Carousal Data                  ==================================
    // =====================================================================================================================
    private fun getCategoryData() {
        for (i in catImageList.indices) {
            val categoryDataClass =
                CategoryDataClass(catIdList[i], catTitleList[i], catImageList[i])
            categoryDataList.add(categoryDataClass)
        }
        binding.recycleCategory.adapter = CategoryAdapter(categoryDataList, requireActivity())
    }

    private fun setUpCarouselRecyclerView() {
        CarouselSnapHelper().attachToRecyclerView(binding.carouselRecyclerView)
        val images = getImages()
        Log.d("ExploreFragment", "Images to load: $images") // Debugging line
        binding.carouselRecyclerView.adapter = CarouselAdapter(images)
    }

    private fun getImages(): List<String> {
        return listOf(
            "https://i.ibb.co.com/WcrBR30/slider-1.jpg",
            "https://i.ibb.co.com/Xz5PPgG/slider-2.jpg",
            "https://i.ibb.co.com/rsdV5St/slider-3.jpg"
        )
    }

    // =====================================================================================================================
    // =====================================       Get Book Data                  ======================================
    // =====================================================================================================================
    private fun fetchBooks() {
        Log.d(tagLog, "fetchBooks: isCalled")
        booksCollection.orderBy("TimeStamp", Query.Direction.DESCENDING) // Or DESCENDING for reverse order
            .get().addOnSuccessListener { querySnapshot ->
                val books = querySnapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                bookGridAdapter.submitList(books)
            }.addOnFailureListener { e ->
                Toast.makeText(requireActivity(), "Failed to fetch books: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    //=================================


    private fun fetchNotices() {
        db?.collection("polytechnic_notice")
            ?.orderBy("timestamp", Query.Direction.DESCENDING) // Sort by timestamp
            ?.limit(5)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle Firestore error
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val notices = mutableListOf<Notice>()
                    for (document in snapshot.documents) {
                        val notice = document.toObject<Notice>()
                        if (notice != null) {
                            notice.id = document.id // Set the Firestore document ID
                            checkUserLikeStatus(notice) // Check and set user like status
                            notices.add(notice)
                        }
                    }
                    noticeQuoteAdapter.updateNoticeList(notices) // Update the adapter
                }
            }
    }

    private fun checkUserLikeStatus(notice: Notice) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db?.collection("polytechnic_notice")
            ?.document(notice.id)
            ?.collection("likes")
            ?.document(currentUserUid)
            ?.get()
            ?.addOnSuccessListener { document ->
                // Check if the user has liked this notice
                notice.userLiked = document.getBoolean("liked") ?: false
                noticeQuoteAdapter.notifyDataSetChanged()
            }
    }

    //=================================
    override fun onResume() {
        super.onResume()
        // If data is available then Shimmer Effect will be gone
        fetchBooks()
    }
}