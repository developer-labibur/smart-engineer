package com.rudhashi.smartengineer.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.NoticeAdapter
import com.rudhashi.smartengineer.databinding.FragmentLoginBinding
import com.rudhashi.smartengineer.databinding.FragmentNoticeBinding
import com.rudhashi.smartengineer.model.Notice

class NoticeFragment : Fragment() {

    private lateinit var binding: FragmentNoticeBinding
    private lateinit var dialogView: View
    private var db : FirebaseFirestore? = null
    private val noticeList = mutableListOf<Notice>()
    // RecyclerView Adapter
    private lateinit var noticeAdapter: NoticeAdapter // Replace with your RecyclerView adapter
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNoticeBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()
        noticeAdapter = NoticeAdapter(emptyList(), requireActivity())


        // Set up RecyclerView (assuming you have a RecyclerView with ID recyclerView)
        binding.rvNotice.adapter = noticeAdapter
        binding.rvNotice.layoutManager = LinearLayoutManager(requireActivity())

        // Fetch notices from Firestore, ordered by timestamp in descending order
        fetchNotices()

        return binding.root
    }

    private fun fetchNotices() {
        db?.collection("polytechnic_notice")
            ?.orderBy("timestamp", Query.Direction.DESCENDING) // Sort by timestamp
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
                    noticeAdapter.updateNoticeList(notices) // Update the adapter
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
/*
    override fun onStart() {
        super.onStart()
        setupFirestoreListener()
    }

    private fun setupFirestoreListener() {
        listenerRegistration = firestoreDB?.collection("polytechnic_notice")
            ?.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Order by timestamp in descending order
            ?.addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle the error
                    Toast.makeText(requireActivity(), "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Update the RecyclerView with the latest data
                if (snapshots != null) {
                    val updatedList = snapshots.documents.map { document ->
                        document.toObject(Notice::class.java)!! // Map each document to your data model
                    }
                    adapter.updateNoticeList(updatedList) // Update the adapter with the new list
                }
            }
    }

    override fun onStop() {
        super.onStop()
        listenerRegistration?.remove()
    }
*/
}