package com.rudhashi.smartengineer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.rudhashi.smartengineer.databinding.ItemNoticeQuoteBinding
import com.rudhashi.smartengineer.model.Notice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.old.util.formatTimestampToRelativeTime

class NoticePagerAdapter(
    private var noticeList: List<Notice>,
    private val context: Context
) : RecyclerView.Adapter<NoticePagerAdapter.NoticeViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val binding = ItemNoticeQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(noticeList[position])
    }

    override fun getItemCount(): Int = noticeList.size

    inner class NoticeViewHolder(private val binding: ItemNoticeQuoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notice: Notice) {
            binding.apply {
                tvNoticeQText.text = notice.notice
                tvNoticeQTitle.text = notice.category
                tvLovesQCount.text = context.getString(R.string.loves, notice.likes.toString())

                // Like button visibility based on userLiked
                if (notice.userLiked) {
                    btnQLove.visibility = View.INVISIBLE
                    btnQLoved.visibility = View.VISIBLE
                } else {
                    btnQLove.visibility = View.VISIBLE
                    btnQLoved.visibility = View.INVISIBLE
                }

                btnQLove.setOnClickListener { toggleLike(notice) }
                btnQLoved.setOnClickListener { toggleLike(notice) }

                // Format and display the timestamp
                tvNoticeQDate.text = formatTimestampToRelativeTime(notice.timestamp!!)

                // Set up menu
                menuQImageView.setOnClickListener { showPopupMenu(notice) }
            }
        }

        private fun showPopupMenu(notice: Notice) {
            val popupMenu = PopupMenu(binding.root.context, binding.menuQImageView)
            popupMenu.inflate(R.menu.menu_notice_item)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.share_note -> {
                        shareNotice(notice.notice)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun shareNotice(noticeText: String) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, noticeText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share this Note via"))
        }

        private fun toggleLike(notice: Notice) {
            if (currentUserUid == null) return

            val noticeRef = db.collection("polytechnic_notice").document(notice.id)
            val userLikeRef = noticeRef.collection("likes").document(currentUserUid)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(noticeRef)
                val currentLikes = snapshot.getLong("likes") ?: 0

                if (notice.userLiked) {
                    transaction.update(noticeRef, "likes", currentLikes - 1)
                    transaction.delete(userLikeRef)
                    notice.userLiked = false
                    notice.likes -= 1
                } else {
                    transaction.update(noticeRef, "likes", currentLikes + 1)
                    transaction.set(userLikeRef, mapOf("liked" to true), SetOptions.merge())
                    notice.userLiked = true
                    notice.likes += 1
                }
                transaction
            }.addOnSuccessListener {
                binding.tvLovesQCount.text = context.getString(R.string.loves, notice.likes.toString())
                if (notice.userLiked) {
                    binding.btnQLove.visibility = View.INVISIBLE
                    binding.btnQLoved.visibility = View.VISIBLE
                } else {
                    binding.btnQLove.visibility = View.VISIBLE
                    binding.btnQLoved.visibility = View.INVISIBLE
                }
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }
        }
    }
}