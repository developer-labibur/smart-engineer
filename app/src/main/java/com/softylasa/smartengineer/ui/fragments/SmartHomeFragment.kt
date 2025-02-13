package com.softylasa.smartengineer.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.models.Browse
import com.softylasa.smartengineer.utils.helpers.GetSet.lToast
import com.softylasa.smartengineer.ui.adapters.CategoryAdapter
import com.softylasa.smartengineer.databinding.DialogMarqueeNoticeBinding
import com.softylasa.smartengineer.databinding.FragmentSmartHomeBinding
import com.softylasa.smartengineer.managers.DialogManager.showFreemiumFeaturesDialog
import com.softylasa.smartengineer.data.models.Category
import com.softylasa.smartengineer.data.models.Features
import com.softylasa.smartengineer.data.models.Notice
import com.softylasa.smartengineer.managers.DialogManager.comingSoon
import com.softylasa.smartengineer.ui.activities.AcademicBookActivity
import com.softylasa.smartengineer.utils.helpers.Converters.formatTimestampToRelativeTime
import com.softylasa.smartengineer.ui.activities.DepartmentActivity
import com.softylasa.smartengineer.ui.activities.LinksActivity
import com.softylasa.smartengineer.ui.activities.NoticeBoardActivity
import com.softylasa.smartengineer.ui.activities.ProfileActivity
import com.softylasa.smartengineer.ui.activities.SearchActivity
import com.softylasa.smartengineer.ui.adapters.BrowseAdapter
import com.softylasa.smartengineer.ui.adapters.FeatureAdapter

class SmartHomeFragment : Fragment() {

    private lateinit var binding: FragmentSmartHomeBinding
    private val tagName = "SmartHomeFragment"
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var browseAdapter: BrowseAdapter
    private var firestoreDB: FirebaseFirestore? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseRef = FirebaseDatabase.getInstance().getReference("UsersDB/$currentUserUid/Favourites/Notice")
    private val loveCountRef = FirebaseDatabase.getInstance().getReference("Count/NoticeLove")

    private lateinit var featuresDataList: ArrayList<Features>
    private val browseList = mutableListOf<Browse>()

    // Variable for String Array ---------------------------------------------------------------------------------
    private lateinit var appFeatureId: Array<String>
    private lateinit var appFeatureTitle: Array<String>
    private lateinit var appFeatureImageUrl: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmartHomeBinding.inflate(inflater, container, false)
        firestoreDB = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        activity?.let { context ->
            categoryAdapter = CategoryAdapter(emptyList(), context)
            browseAdapter = BrowseAdapter(browseList, context)

            binding.recycleBrowser.layoutManager = LinearLayoutManager(context)
            browseAdapter = BrowseAdapter(browseList, context)
            binding.recycleBrowser.setHasFixedSize(true)
            binding.recycleBrowser.isNestedScrollingEnabled = false
            binding.recycleBrowser.adapter = browseAdapter
            getBrowseData()
        }

        // String Array Hooks ------------------------------------------------------------------------------------
        appFeatureId = resources.getStringArray(R.array.appFeatureId)
        appFeatureTitle = resources.getStringArray(R.array.appFeatureTitle)
        appFeatureImageUrl = resources.getStringArray(R.array.appFeatureImageUrl)

        // Adapter RecyclerView Category -----------------------------------------------------------------------------------
        activity?.let { context ->
            binding.recycleFeatures.layoutManager =
                GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
            binding.recycleFeatures.setHasFixedSize(true)
            binding.recycleFeatures.isNestedScrollingEnabled = false;
            featuresDataList = arrayListOf<Features>()
            getFeaturesData()
        }

        // Enable the marquee effect
        binding.apply {
            marqueeTextView.isSelected = true // Must be true for marquee to work
            marqueeTextView.isSingleLine = true
            marqueeTextView.ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeTextView.marqueeRepeatLimit = -1 // Infinite scrolling
            fetchMarqueeNotices()
        }

        // Button Click ----------------------------------------------------------------
        binding.apply {
            activity?.let { context ->
                btnViewProfile.setOnClickListener { startActivity(Intent(context, ProfileActivity::class.java)) }
                btnViewPremium.setOnClickListener { showFreemiumFeaturesDialog(context) }
            }


            //btnRoutine.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            //btnNotify.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            //btnPosts.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            //btnFavourites.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            //btnIslamic.setOnClickListener { SmartData.comingSoon(requireActivity()) }
        }

        // Set item click listener ----------------------------------------------------------------
        browseAdapter.setOnItemClickListener(object : BrowseAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, name: String) {
                activity?.let { context ->
                    when (name) {
                        "Important Links" -> {
                            val intent = Intent(context, LinksActivity::class.java)
                            intent.putExtra("name", name)
                            intent.putExtra("database", "important_links")
                            startActivity(intent)
                        }
                        "Social Links" -> {
                            val intent = Intent(context, LinksActivity::class.java)
                            intent.putExtra("name", name)
                            intent.putExtra("database", "social_links")
                            startActivity(intent)
                        }
                        else -> comingSoon(context)
                    }
                }
            }
        })

        return binding.root
    }

    // =====================================================================================================================
    // =====================================       Get Category Data                  ======================================
    // =====================================================================================================================
    private fun getFeaturesData() {
        activity?.let { context ->
            for (i in appFeatureImageUrl.indices) {
                val categoryDataClass =
                    Features(appFeatureId[i], appFeatureTitle[i], appFeatureImageUrl[i])
                featuresDataList.add(categoryDataClass)

                binding.recycleFeatures.adapter = FeatureAdapter(featuresDataList, context)

                // Set the click listener
                categoryAdapter.setOnItemClickListener(object :
                    CategoryAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int, dataList: Category) {
                        when (position) {
                            1 -> startActivity(Intent(context, NoticeBoardActivity::class.java))
                            2 -> startActivity(Intent(context, AcademicBookActivity::class.java))
                            3 -> startActivity(Intent(context, SearchActivity::class.java))
                            else -> comingSoon(context)
                        }

                        val intent = Intent(context, DepartmentActivity::class.java)
                        intent.putExtra("DCode", dataList.DCode)
                        startActivity(intent)
                    }
                })
            }
        }
    }

    private fun fetchMarqueeNotices() {
        firestoreDB?.collection("polytechnic_notice")
            ?.orderBy("timestamp", Query.Direction.DESCENDING) // Sort by timestamp
            ?.limit(1)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle Firestore error
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val notice = document.toObject<Notice>()
                        if (notice != null) {
                            val category = notice.category
                            val text = notice.notice

                            binding.clMarquee.setOnClickListener {
                                notice.timestamp?.let {
                                    setNoticeDialog(notice, category, it, text)
                                }
                            }

                            val limitedText = text.split(" ").take(15)
                                .joinToString(" ") // Limit to the first 3 words
                            val clickToSeeMore = "...বিস্তারিত পড়তে ক্লিক করুন।"
                            val fullText =
                                "$category $limitedText $clickToSeeMore" // Combine them into one string
                            val spannableString =
                                SpannableString(fullText) // Create a SpannableString
                            // Set the category part in bold
                            spannableString.setSpan(
                                StyleSpan(Typeface.BOLD), // Bold style
                                0, // Start index of category
                                category.length, // End index of category
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            // Set the "...See More" part in bold
                            spannableString.setSpan(
                                StyleSpan(Typeface.BOLD),
                                fullText.length - clickToSeeMore.length, // Start index of "...Click to See More"
                                fullText.length, // End index of "...Click to See More"
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            binding.marqueeTextView.text = spannableString  // Set the spannable string to the TextView
                        }
                    }
                }
            }
    }

    @SuppressLint("InflateParams")
    private fun setNoticeDialog(
        notice: Notice,
        category: String,
        timestamp: Timestamp,
        text: String
    ) {
        activity?.let { context ->
            val builder = AlertDialog.Builder(context).create()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_marquee_notice, null)
            val dialogBinding = DialogMarqueeNoticeBinding.bind(dialogView)
            builder.setView(dialogBinding.root)
            builder.show()

            dialogBinding.apply {
                tvNoticeTitle.text = category
                tvNoticeDate.text = formatTimestampToRelativeTime(timestamp)
                tvNoticeText.text = text
                // Observe loves count for this notice
                loveCountRef.child(notice.id).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val loveCount = snapshot.getValue(Int::class.java) ?: 0
                        tvLovesCount.text = getString(R.string.loves, loveCount.toString())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if needed
                    }
                })

                setUpLoveButton(btnLove, notice)

                menuImageView.setOnClickListener { showCustomPopupMenu(it, notice) }
            }

            if (builder.window != null) builder.window!!.setBackgroundDrawable(ColorDrawable(0))
            builder.show()
        }
    }

    private fun setUpLoveButton(btnLove: AppCompatButton, notice: Notice) {
        // Load the current like state from Firebase Realtime Database
        databaseRef.child(notice.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isLoved = snapshot.getValue(Boolean::class.java) ?: false
                updateLoveButtonState(btnLove, isLoved)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
        binding.apply {
            // Handle Love button click
            btnLove.setOnClickListener {
                databaseRef.child(notice.id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val isLoved = snapshot.getValue(Boolean::class.java) ?: false
                            if (isLoved) {
                                // Remove from favorites
                                databaseRef.child(notice.id).setValue(false)
                                updateLoveButtonState(btnLove, false)

                                // Decrement loves count in Realtime Database
                                loveCountRef.child(notice.id).get()
                                    .addOnSuccessListener { loveSnapshot ->
                                        val currentCount = loveSnapshot.getValue(Int::class.java) ?: 0
                                        loveCountRef.child(notice.id).setValue(currentCount - 1)
                                    }
                            } else {
                                // Add to favorites
                                databaseRef.child(notice.id).setValue(true)
                                updateLoveButtonState(btnLove, true)

                                // Increment loves count in Realtime Database
                                loveCountRef.child(notice.id).get()
                                    .addOnSuccessListener { loveSnapshot ->
                                        val currentCount = loveSnapshot.getValue(Int::class.java) ?: 0
                                        loveCountRef.child(notice.id).setValue(currentCount + 1)
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error if needed
                        }
                    })
            }
        }
    }

    private fun updateLoveButtonState(loveButton: AppCompatButton, isLoved: Boolean) {
        activity?.let { context ->
            if (isLoved) {
                loveButton.setBackgroundResource(R.drawable.bg_notice_item_loved)
                loveButton.text = getString(R.string.loved)
                loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sl_heart_red, 0, 0, 0)
                loveButton.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.red
                    )
                ) // Set text color to red
            } else {
                loveButton.setBackgroundResource(R.drawable.abc_orange_main)
                loveButton.text = getString(R.string.love)
                loveButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.sl_heart_black,
                    0,
                    0,
                    0
                )
                loveButton.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.ic_light_only
                    )
                ) // Set text color to red
            }
        }

    }

    @SuppressLint("InflateParams")
    private fun showCustomPopupMenu(anchorView: View, notice: Notice) {
        // Inflate the custom menu layout
        val popupView =
            LayoutInflater.from(anchorView.context).inflate(R.layout.popup_menu_layout, null)
        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ) // Focusable, so it will close when touched outside
        // Set custom background drawable (optional if background is set in XML)
        activity?.let { context ->
            popupWindow.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.menu_background
                )
            )
        }
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
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, noticeText)
            type = "text/plain"
        }
        requireContext().startActivity(Intent.createChooser(shareIntent, "Share this Note via"))
    }

    // =====================================================================================================================
    // ===========================================       Get Browse Data     =============================================
    // =====================================================================================================================
    @SuppressLint("NotifyDataSetChanged")
    private fun getBrowseData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("browse")   // Reference to 'browser' collection
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d(tagName, "No documents found!")
                } else {
                    browseList.clear() // ✅ Clear list to prevent duplicates

                    for (document in result) {
                        val browse = document.toObject(Browse::class.java)
                        Log.d(tagName, "Fetched data: ${browse.Name}, ${browse.Cover}, ${browse.Des}")
                        browseList.add(browse)
                    }

                    browseAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                activity?.let { context ->
                    lToast(context, "Error getting documents: ${exception.message}")
                }
            }
    }
}