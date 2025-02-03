package com.rudhashi.smartengineer.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.ui.activities.SmartDashboardActivity
import com.rudhashi.smartengineer.ui.adapters.BookGridAdapter
import com.rudhashi.smartengineer.ui.adapters.CarouselAdapter
import com.rudhashi.smartengineer.ui.adapters.NoticeQuoteAdapter
import com.rudhashi.smartengineer.databinding.FragmentHomeBinding
import com.rudhashi.smartengineer.data.models.Book
import com.rudhashi.smartengineer.data.models.Category
import com.rudhashi.smartengineer.data.models.Notice
import com.rudhashi.smartengineer.ui.activities.ProfileActivity

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var categoryDataList: ArrayList<Category>
    private lateinit var catImageList: Array<String>
    private lateinit var catTitleList: Array<String>
    private lateinit var catIdList: Array<String>
    private lateinit var bookGridAdapter: BookGridAdapter
    private lateinit var noticeQuoteAdapter: NoticeQuoteAdapter
    private var db: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        noticeQuoteAdapter = NoticeQuoteAdapter(emptyList(), requireActivity())

        setGradientTextColor()

        // Category or Department Set Up -----------------------------------------------------------------------------------
        binding.apply {
            recycleCategory.layoutManager = GridLayoutManager(requireActivity(), 3, GridLayoutManager.VERTICAL, false)
            recycleCategory.setHasFixedSize(true)
            recycleCategory.isNestedScrollingEnabled = false
            categoryDataList = arrayListOf<Category>()
            getCategoryData()
        }

        // CarouselSet Up -----------------------------------------------------------------------------------
        setUpCarouselRecyclerView()

        // Book Grid Set Up -----------------------------------------------------------------------------------
        bookGridAdapter = BookGridAdapter { bookCode ->
            val intent = Intent(requireContext(), ProfileActivity::class.java)

            intent.putExtra("BOOK_CODE", bookCode)
            // Start the Activity with the options
            startActivity(intent)
        }
        binding.apply {
            recycleBooks.layoutManager = GridLayoutManager(requireActivity(),1, GridLayoutManager.HORIZONTAL,false)
            recycleBooks.adapter = bookGridAdapter

            btnMyLibrary.setOnClickListener { fetchBooks() }
            btnMyFavorites.setOnClickListener { fetchBooks() }
        }

        // Notice Quote Set Up -----------------------------------------------------------------------------------
        binding.viewPager.adapter = noticeQuoteAdapter

        // Set height based on the currently selected page
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.viewPager.post {
                    updatePagerHeight(position)
                }
                binding.btnMyLibrary.text = position.toString()
            }
        })
        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.btnMyLibrary.setOnClickListener { startActivity(Intent(requireActivity(), SmartDashboardActivity::class.java)) }

        binding.btnProfile.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_profileActivity)
        }

        return binding.root
    }

    // =====================================================================================================================
    // =====================     Gradient Text color  &  ViewPager Dynamic Height                  =========================
    // =====================================================================================================================
    private fun setGradientTextColor(){
        binding.apply {
            // Gradient Text color Set -----------------------------------------------------------------------------------------
            tvSmart.paint.shader = LinearGradient(0f, 0f, tvSmart.paint.measureText(tvSmart.text.toString()), tvSmart.textSize, intArrayOf(
                resources.getColor(R.color.gradient_start, null),
                resources.getColor(R.color.gradient_start, null),
                resources.getColor(R.color.gradient_end, null)
            ), null, Shader.TileMode.CLAMP)
            tvE.paint.shader = LinearGradient(0f, 0f, tvE.paint.measureText(tvE.text.toString()), tvE.textSize, intArrayOf(
                resources.getColor(R.color.clr2_1, null),
                resources.getColor(R.color.clr1_1, null),
                resources.getColor(R.color.clr2_1, null)
            ), null, Shader.TileMode.CLAMP)
            tvEngineer.paint.shader = LinearGradient(0f, 0f, tvEngineer.paint.measureText(tvEngineer.text.toString()), tvEngineer.textSize, intArrayOf(
                resources.getColor(R.color.clr2_1_1, null),
                resources.getColor(R.color.clr2_1_2, null),
                resources.getColor(R.color.clr2_1_3, null)
            ), null, Shader.TileMode.CLAMP)
            tvPoly.paint.shader = LinearGradient(0f, 0f, tvPoly.paint.measureText(binding.tvPoly.text.toString()), tvPoly.textSize, intArrayOf(
                resources.getColor(R.color.clr2_1_2, null),
                resources.getColor(R.color.clr2_1_1, null),
                resources.getColor(R.color.clr2_1_1, null)
            ), null, Shader.TileMode.CLAMP)
        }
    }
    private fun updatePagerHeight(position: Int) {
        val view = (binding.viewPager[0] as RecyclerView).layoutManager?.findViewByPosition(position)
        view?.let {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            val height = view.measuredHeight
            if (binding.viewPager.layoutParams.height != height) {
                binding.viewPager.layoutParams = binding.viewPager.layoutParams.apply {
                    this.height = height
                }
            }
        }
    }

    // =====================================================================================================================
    // ============================       Get Category and Carousal Data                  ==================================
    // =====================================================================================================================
    private fun getCategoryData() {
        //binding.recycleCategory.adapter = CategoryAdapter(categoryDataList, requireActivity())
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
    // =======================================       Get Book Data                  ========================================
    // =====================================================================================================================
    private fun fetchBooks() {
        db?.collection("books")?.orderBy("TimeStamp", Query.Direction.DESCENDING) // Or DESCENDING for reverse order
            ?.get()?.addOnSuccessListener { querySnapshot ->
                val books = querySnapshot.documents.mapNotNull { it.toObject(Book::class.java) }
                bookGridAdapter.setData(books, true)
            }?.addOnFailureListener { e ->
                Toast.makeText(requireActivity(), "Failed to fetch books: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // =====================================================================================================================
    // =======================================       Notice Quote                  =========================================
    // =====================================================================================================================
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
    @SuppressLint("NotifyDataSetChanged")
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

    //=======================================================================================================================
    override fun onResume() {
        super.onResume()
        fetchBooks()
        fetchNotices()
    }

}