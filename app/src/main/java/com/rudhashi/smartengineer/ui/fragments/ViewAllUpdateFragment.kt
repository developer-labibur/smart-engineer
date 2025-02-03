package com.rudhashi.smartengineer.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.rudhashi.smartengineer.ui.adapters.NoticeAdapter
import com.rudhashi.smartengineer.databinding.FragmentViewAllUpdateBinding
import com.rudhashi.smartengineer.data.models.Notice
import com.rudhashi.smartengineer.utils.helpers.SmartData

class ViewAllUpdateFragment : Fragment() {

    private lateinit var binding: FragmentViewAllUpdateBinding
    private var db : FirebaseFirestore? = null

    // RecyclerView Adapter
    private lateinit var noticeAdapter: NoticeAdapter // Replace with your RecyclerView adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentViewAllUpdateBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()
        noticeAdapter = NoticeAdapter(emptyList(), requireActivity())

        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        binding.rvNotice.adapter = noticeAdapter
        binding.rvNotice.layoutManager = LinearLayoutManager(requireActivity())

        // Fetch notices from Firestore, ordered by timestamp in descending order
        fetchNotices()
        isShimmerOn(true)

        binding.apply {
            btnNotice.setOnClickListener {
                fetchNotices()
                isShimmerOn(true)
            }
            btnBlog.setOnClickListener { SmartData.comingSoon(requireActivity()) }
        }

        return binding.root
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