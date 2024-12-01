package com.rudhashi.smartengineer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ItemNoticeBinding
import com.rudhashi.smartengineer.model.Notice
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.formatTimestampToRelativeTime

class NoticeAdapter(private var noticeList: List<Notice>, var context: Context) :
    RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    // ViewHolder class to hold the views
    private val db = FirebaseFirestore.getInstance()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val binding = ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeViewHolder(binding)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(noticeList[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int = noticeList.size

    // Update the notice list with new data
    @SuppressLint("NotifyDataSetChanged")
    fun updateNoticeList(newNoticeList: List<Notice>) {
        noticeList = newNoticeList
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    inner class NoticeViewHolder(private val binding: ItemNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind the notice data to the views
        fun bind(notice: Notice) {
            binding.apply {
                tvNoticeText.text = notice.notice
                tvNoticeTitle.text = notice.category
                tvLovesCount.text = context.getString(R.string.loves, notice.likes.toString())

                // Set initial like button text based on userLiked
                if (notice.userLiked) {
                    btnLove.visibility = View.INVISIBLE
                    btnLoved.visibility = View.VISIBLE
                } else {
                    btnLove.visibility = View.VISIBLE
                    btnLoved.visibility = View.INVISIBLE
                }

                btnLove.setOnClickListener { toggleLike(notice) }
                btnLoved.setOnClickListener { toggleLike(notice) }



                // Random Gradient Background Color Programmatically
                val number = SmartData.randomNumber(6)
                when (number) {
                    1 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_blue_main)
                        binding.btnLove.setBackgroundResource(R.drawable.abc_blue_main)
                        binding.clickNoticeItem.setBackgroundResource(R.drawable.abc_blue_light)
                    }
                    2 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_green_main)
                        binding.btnLove.setBackgroundResource(R.drawable.abc_green_main)
                        binding.clickNoticeItem.setBackgroundResource(R.drawable.abc_green_light)
                    }
                    3 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_pink_main)
                        binding.btnLove.setBackgroundResource(R.drawable.abc_pink_main)
                        binding.clickNoticeItem.setBackgroundResource(R.drawable.abc_pink_light)
                    }
                    4 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_orange_main)
                        binding.btnLove.setBackgroundResource(R.drawable.abc_orange_main)
                        binding.clickNoticeItem.setBackgroundResource(R.drawable.abc_orange_light)
                    }
                    5 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_purple_main)
                        binding.btnLove.setBackgroundResource(R.drawable.abc_purple_main)
                        binding.clickNoticeItem.setBackgroundResource(R.drawable.abc_purple_light)
                    }
                    6 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_red_main)
                        binding.btnLove.setBackgroundResource(R.drawable.abc_red_main)
                        binding.clickNoticeItem.setBackgroundResource(R.drawable.abc_red_light)
                    }
                    else -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_blue_main)
                        binding.btnLove.setBackgroundResource(R.drawable.abc_blue_main)
                        binding.clickNoticeItem.setBackgroundResource(R.drawable.abc_blue_light)
                    }
                }

                // Format and display the timestamp
                val formattedTime = formatTimestampToRelativeTime(notice.timestamp!!)
                tvNoticeDate.text = formattedTime

                // Set up three-dot menu
                menuImageView.setOnClickListener { showCustomPopupMenu(it, notice) }
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

            // Get the screen width
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            // Set your desired right margin
            val rightMargin = 16 // Adjust this value to set the margin you want

            // Calculate x position to align the right endpoints
            // x position should be the right edge of the anchor view minus the popup width minus the desired right margin
            val xPos = location[0] + anchorView.width - popupWidth - rightMargin

            // Show the PopupWindow at the calculated position
            popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos, location[1])
        }

        private fun showPopupMenu(notice: Notice) {
            val popupMenu = PopupMenu(binding.root.context, binding.menuImageView)
            val inflater: MenuInflater = popupMenu.menuInflater
            inflater.inflate(R.menu.menu_notice_item, popupMenu.menu)

            // Adjust menu location using offset if it clips
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.share_note -> {
                        shareNotice(notice.notice)
                        true
                    }

                    else -> false
                }
            }

            // Show the menu with an offset if it’s close to the screen edge
            binding.menuImageView.post {
                val screenWidth = binding.root.context.resources.displayMetrics.widthPixels
                val location = IntArray(2)
                binding.menuImageView.getLocationOnScreen(location)

                // Calculate the remaining space on the right side of the menu
                val remainingSpace = screenWidth - location[0]

                // If remaining space is too narrow, apply offset
                if (remainingSpace < 200) { // Adjust 200 to control how much space is needed
                    popupMenu.gravity = Gravity.END
                }

                popupMenu.show()
            }
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
                    deductPoints(SmartConfig.UNLIKE_DEDUCT_POINT)
                } else {
                    // Like the post: increment likes and update Firestore
                    transaction.update(noticeRef, "likes", currentLikes + 1)
                    transaction.set(userLikeRef, mapOf("liked" to true), SetOptions.merge())
                    notice.userLiked = true
                    notice.likes += 1
                    addPoints(SmartConfig.LIKED_BONUS_POINT)
                }

                transaction
            }.addOnSuccessListener {
                binding.apply {
                    // Update UI to reflect the new like count and button text
                    tvLovesCount.text = context.getString(R.string.loves, notice.likes.toString())
                    if (notice.userLiked) {
                        btnLove.visibility = View.INVISIBLE
                        btnLoved.visibility = View.VISIBLE
                    } else {
                        btnLove.visibility = View.VISIBLE
                        btnLoved.visibility = View.INVISIBLE
                    }
                }
            }.addOnFailureListener { e ->
                // Handle failure, such as network issues
                e.printStackTrace()
            }
        }
    }

    fun addPoints(pointsToAdd: Int) {
        val database = Firebase.database
        val userRef = database.getReference("Users/$currentUserUid/points")
        userRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
            val newPoints = currentPoints + pointsToAdd
            userRef.setValue(newPoints)
        }
    }
    private fun deductPoints(pointsToDeduct: Int) {
        val database = Firebase.database
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val userRef = database.getReference("Users/$userId/points")
        userRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
            if (currentPoints >= pointsToDeduct) {
                val newPoints = currentPoints - pointsToDeduct
                userRef.setValue(newPoints)
            }
        }
    }
}