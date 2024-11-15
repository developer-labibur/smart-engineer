package com.rudhashi.smartengineer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.rudhashi.smartengineer.adapter.NoticeQuoteAdapter
import com.rudhashi.smartengineer.databinding.ActivityTestBinding
import com.rudhashi.smartengineer.model.Notice

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private lateinit var noticeQuoteAdapter: NoticeQuoteAdapter

    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        noticeQuoteAdapter = NoticeQuoteAdapter(emptyList(), this)


        binding.viewPager.adapter = noticeQuoteAdapter

        fetchNotices()

        // Set height based on the currently selected page
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.viewPager.post {
                    updatePagerHeight(position)
                }
            }
        })
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
}