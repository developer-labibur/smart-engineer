package com.rudhashi.smartengineer.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ItemNoticeQuoteBinding
import com.rudhashi.smartengineer.model.Notice
import com.rudhashi.smartengineer.util.formatTimestampToRelativeTime

class NoticeQuoteAdapter(private var noticeList: List<Notice>, var context: Context) :
    RecyclerView.Adapter<NoticeQuoteAdapter.NoticeQuoteViewHolder>() {

    // ViewHolder class to hold the views
    private val db = FirebaseFirestore.getInstance()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    inner class NoticeQuoteViewHolder(private val binding: ItemNoticeQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notice: Notice) {
            binding.apply {
                tvNoticeQText.text = notice.notice
                tvNoticeQTitle.text = notice.category
                tvLovesQCount.text = context.getString(R.string.loves, notice.likes.toString())

                // Set initial like button text based on userLiked
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
                val formattedTime = formatTimestampToRelativeTime(notice.timestamp!!)
                tvNoticeQDate.text = formattedTime

                // Set up three-dot menu
                menuQImageView.setOnClickListener { showCustomPopupMenu(it, notice) }
            }
        }
        private fun showCustomPopupMenu(anchorView: View, notice: Notice) {
            // Inflate the custom menu layout
            val popupView =
                LayoutInflater.from(anchorView.context).inflate(R.layout.popup_menu_layout, null)

            // Create the PopupWindow
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // Focusable, so it will close when touched outside
            )

            // Set custom background drawable (optional if background is set in XML)
            popupWindow.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.menu_background
                )
            )

            // Handle menu item clicks
            val shareNote = popupView.findViewById<LinearLayout>(R.id.share_note)
            shareNote.setOnClickListener {
                shareNotice(notice.notice)
                popupWindow.dismiss() // Close the popup after click
            }

            // Measure the width of the popup to calculate the right margin
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val popupWidth = popupView.measuredWidth

            // Get the location of the anchor view (three-dot icon)
            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)

            // Set your desired right margin
            val rightMargin = 16 // Adjust this value to set the margin you want

            // Calculate x position to align the right endpoints
            // x position should be the right edge of the anchor view minus the popup width minus the desired right margin
            val xPos = location[0] + anchorView.width - popupWidth - rightMargin

            // Show the PopupWindow at the calculated position
            popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos, location[1])
        }

        private fun shareNotice(noticeText: String) {
            val context = itemView.context
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
                    // Unlike the post: decrement likes and update Firestore
                    transaction.update(noticeRef, "likes", currentLikes - 1)
                    transaction.delete(userLikeRef)
                    notice.userLiked = false
                    notice.likes -= 1
                } else {
                    // Like the post: increment likes and update Firestore
                    transaction.update(noticeRef, "likes", currentLikes + 1)
                    transaction.set(userLikeRef, mapOf("liked" to true), SetOptions.merge())
                    notice.userLiked = true
                    notice.likes += 1
                }

                transaction
            }.addOnSuccessListener {
                binding.apply {
                    // Update UI to reflect the new like count and button text
                    tvLovesQCount.text = context.getString(R.string.loves, notice.likes.toString())
                    if (notice.userLiked) {
                        btnQLove.visibility = View.INVISIBLE
                        btnQLoved.visibility = View.VISIBLE
                    } else {
                        btnQLove.visibility = View.VISIBLE
                        btnQLoved.visibility = View.INVISIBLE
                    }
                }
            }.addOnFailureListener { e ->
                // Handle failure, such as network issues
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeQuoteViewHolder {
        val binding = ItemNoticeQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeQuoteViewHolder(binding)
    }


    override fun onBindViewHolder(holder: NoticeQuoteViewHolder, position: Int) {
        holder.bind(noticeList[position])
    }

    override fun getItemCount(): Int = noticeList.size

    // Update the notice list with new data
    @SuppressLint("NotifyDataSetChanged")
    fun updateNoticeList(newNoticeList: List<Notice>) {
        noticeList = newNoticeList
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}