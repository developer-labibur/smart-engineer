package com.softylasa.smartengineer.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.softylasa.smartengineer.data.models.Notice
import com.softylasa.smartengineer.databinding.ActivityNewsfeedBinding
import com.softylasa.smartengineer.ui.adapters.NoticeAdapter
import com.softylasa.smartengineer.utils.themes.SmartThemeActivity

class NewsfeedActivity : SmartThemeActivity() {

    private lateinit var binding: ActivityNewsfeedBinding
    private var db : FirebaseFirestore? = null

    // RecyclerView Adapter
    private lateinit var noticeAdapter: NoticeAdapter // Replace with your RecyclerView adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewsfeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        noticeAdapter = NoticeAdapter(emptyList(), this)

        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        binding.rvNotice.adapter = noticeAdapter
        binding.rvNotice.layoutManager = LinearLayoutManager(this)

        // Fetch notices from Firestore, ordered by timestamp in descending order
        fetchNotices()
        isShimmerOn(true)

        binding.btnBack.setOnClickListener { finish() }
    }


    // =====================================================================================================================
    // =======================================       Get Notice Data                  ========================================
    // =====================================================================================================================
    private fun fetchNotices() {
        db?.collection("polytechnic_notice")
            ?.orderBy("timestamp", Query.Direction.DESCENDING) // Sort by timestamp
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isShimmerOn(false)
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
                            isShimmerOn(false)
                        }
                    }
                    noticeAdapter.updateNoticeList(notices, false) // Update the adapter
                    // Ensure data is loaded before setting the height

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
                noticeAdapter.notifyDataSetChanged()
            }
    }

    override fun onPause() {
        super.onPause()
        isShimmerOn(false)
    }

    private fun isShimmerOn(flag: Boolean){
        binding.apply {
            if (flag){
                shimmerNotice.startShimmer()
                shimmerNotice.visibility = View.VISIBLE
                rvNotice.visibility = View.GONE
            } else {
                shimmerNotice.stopShimmer()
                shimmerNotice.visibility = View.GONE
                rvNotice.visibility = View.VISIBLE
            }
        }
    }

}