package com.rudhashi.smartengineer.fragment

import android.annotation.SuppressLint
import android.content.Intent
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
import com.rudhashi.smartengineer.adapter.NoticeAdapter
import com.rudhashi.smartengineer.databinding.FragmentSmartUpdatesBinding
import com.rudhashi.smartengineer.model.Notice
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.view.ProfileActivity
import com.rudhashi.smartengineer.view.SmartPointActivity
import com.rudhashi.smartengineer.view.SmartViewActivity

class SmartUpdatesFragment : Fragment() {

    private lateinit var binding: FragmentSmartUpdatesBinding
    private var db : FirebaseFirestore? = null

    // RecyclerView Adapter
    private lateinit var noticeAdapter: NoticeAdapter // Replace with your RecyclerView adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmartUpdatesBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()
        noticeAdapter = NoticeAdapter(emptyList(), requireActivity())

        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        binding.rvNotice.adapter = noticeAdapter
        binding.rvNotice.layoutManager = LinearLayoutManager(requireActivity())

        // Fetch notices from Firestore, ordered by timestamp in descending order
        fetchNotices()
        isShimmerOn(true)

        binding.apply {
            btnViewProfile.setOnClickListener { startActivity(Intent(requireActivity(), ProfileActivity::class.java)) }
            btnViewPremium.setOnClickListener { startActivity(Intent(requireActivity(), SmartPointActivity::class.java)) }

            btnNotice.setOnClickListener {
                fetchNotices()
                isShimmerOn(true)
            }
            btnBlog.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            btnViewAll.visibility = View.GONE
            btnViewAll.setOnClickListener {
                val intent = Intent(requireContext(), SmartViewActivity::class.java)
                intent.putExtra("DISPLAY", "Update")
                startActivity(intent)
            }
        }

        return binding.root
    }

    private fun fetchNotices() {
        db?.collection("polytechnic_notice")
            ?.orderBy("timestamp", Query.Direction.DESCENDING) // Sort by timestamp
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    binding.btnViewAll.visibility = View.VISIBLE
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
                            binding.btnViewAll.visibility = View.VISIBLE
                        }
                    }
                    noticeAdapter.updateNoticeList(notices, true) // Update the adapter
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
                updateContainer.visibility = View.GONE
            } else {
                shimmerNotice.stopShimmer()
                shimmerNotice.visibility = View.GONE
                updateContainer.visibility = View.VISIBLE
            }
        }
    }

}