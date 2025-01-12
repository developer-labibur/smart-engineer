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
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ItemNoticeBinding
import com.rudhashi.smartengineer.model.Notice
import com.rudhashi.smartengineer.old.util.SmartConfig
import com.rudhashi.smartengineer.old.util.SmartData
import com.rudhashi.smartengineer.old.util.formatTimestampToRelativeTime
import kotlin.properties.Delegates

class NoticeAdapter(private var noticeList: List<Notice>, var context: Context) :
    RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    // ViewHolder class to hold the views
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseRef = FirebaseDatabase.getInstance().getReference("UsersDB/$currentUserUid/Favourites/Notice")
    private val loveCountRef = FirebaseDatabase.getInstance().getReference("UsersDB/$currentUserUid/Favourites/CountNoticeLove")
    private var isRandom by Delegates.notNull<Boolean>()

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
    fun updateNoticeList(newNoticeList: List<Notice>, isRandom: Boolean) {
        noticeList = newNoticeList
        this.isRandom = isRandom
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    inner class NoticeViewHolder(private val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root) {

        // Bind the notice data to the views
        fun bind(notice: Notice) {
            binding.apply {
                tvNoticeText.text = notice.notice
                tvNoticeTitle.text = notice.category

                // Observe loves count for this notice
                loveCountRef.child(notice.id).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val loveCount = snapshot.getValue(Int::class.java) ?: 0
                        tvLovesCount.text = context.getString(R.string.loves, loveCount.toString())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if needed
                    }
                })

                // Random Gradient Background Color Programmatically
                val number = SmartData.randomNumber(6)
                if (isRandom){
                    when (number) {
                        1 -> setUpLoveButton(btnLove, notice, R.drawable.abc_blue_main, R.drawable.abc_blue_main, R.drawable.abc_blue_light)
                        2 -> setUpLoveButton(btnLove, notice, R.drawable.abc_green_main, R.drawable.abc_green_main, R.drawable.abc_green_light)
                        3 -> setUpLoveButton(btnLove, notice, R.drawable.abc_pink_main, R.drawable.abc_pink_main, R.drawable.abc_pink_light)
                        4 -> setUpLoveButton(btnLove, notice, R.drawable.abc_orange_main, R.drawable.abc_orange_main, R.drawable.abc_orange_light)
                        5 -> setUpLoveButton(btnLove, notice, R.drawable.abc_purple_main, R.drawable.abc_purple_main, R.drawable.abc_purple_light)
                        6 -> setUpLoveButton(btnLove, notice, R.drawable.abc_red_main, R.drawable.abc_red_main, R.drawable.abc_red_light)
                        else -> setUpLoveButton(btnLove, notice, R.drawable.abc_blue_main, R.drawable.abc_blue_main, R.drawable.abc_blue_light)
                    }
                }

                // Format and display the timestamp
                val formattedTime = formatTimestampToRelativeTime(notice.timestamp!!)
                tvNoticeDate.text = formattedTime

                // Set up three-dot menu
                menuImageView.setOnClickListener { showCustomPopupMenu(it, notice) }
            }
        }
        private fun setUpLoveButton(btnLove: AppCompatButton, notice: Notice, btnLoveBg: Int, llItemBGBg: Int, clickNoticeItemBg: Int){
            // Load the current like state from Firebase Realtime Database
            databaseRef.child(notice.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isLoved = snapshot.getValue(Boolean::class.java) ?: false
                    updateLoveButtonState(btnLove, isLoved, btnLoveBg)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
            binding.llItemBG.setBackgroundResource(llItemBGBg)
            binding.clickNoticeItem.setBackgroundResource(clickNoticeItemBg)
            binding.apply {
                // Handle Love button click
                btnLove.setOnClickListener {
                    databaseRef.child(notice.id).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val isLoved = snapshot.getValue(Boolean::class.java) ?: false
                            if (isLoved) {
                                // Remove from favorites
                                databaseRef.child(notice.id).setValue(false)
                                updateLoveButtonState(btnLove, false, btnLoveBg)

                                // Decrement loves count in Realtime Database
                                loveCountRef.child(notice.id).get().addOnSuccessListener { loveSnapshot ->
                                    val currentCount = loveSnapshot.getValue(Int::class.java) ?: 0
                                    loveCountRef.child(notice.id).setValue(currentCount - 1)
                                }
                                deductPoints(SmartConfig.UNLIKE_DEDUCT_POINT)
                            } else {
                                // Add to favorites
                                databaseRef.child(notice.id).setValue(true)
                                updateLoveButtonState(btnLove, true, btnLoveBg)

                                // Increment loves count in Realtime Database
                                loveCountRef.child(notice.id).get().addOnSuccessListener { loveSnapshot ->
                                    val currentCount = loveSnapshot.getValue(Int::class.java) ?: 0
                                    loveCountRef.child(notice.id).setValue(currentCount + 1)
                                }
                                addPoints(SmartConfig.LIKED_BONUS_POINT)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error if needed
                        }
                    })
                }
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
        // Function to update Love button UI state
        private fun updateLoveButtonState(loveButton: AppCompatButton, isLoved: Boolean, bg: Int) {
            if (isLoved) {
                loveButton.setBackgroundResource(R.drawable.bg_notice_item_loved)
                loveButton.text = context.getString(R.string.loved)
                loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sl_heart_red, 0, 0, 0)
                loveButton.setTextColor(ContextCompat.getColor(context, R.color.red)) // Set text color to red
            } else {
                loveButton.setBackgroundResource(bg)
                loveButton.text = context.getString(R.string.love)
                loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sl_heart_black, 0, 0, 0)
                loveButton.setTextColor(ContextCompat.getColor(context, R.color.ic_light_only)) // Set text color to red
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