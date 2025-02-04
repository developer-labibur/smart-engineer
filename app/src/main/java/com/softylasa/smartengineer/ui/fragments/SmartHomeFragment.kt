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
import com.softylasa.smartengineer.utils.helpers.GetSet.lToast
import com.softylasa.smartengineer.utils.helpers.GetSet.sToast
import com.softylasa.smartengineer.ui.adapters.CategoryAdapter
import com.softylasa.smartengineer.ui.adapters.SemesterAdapter
import com.softylasa.smartengineer.databinding.DialogMarqueeNoticeBinding
import com.softylasa.smartengineer.databinding.FragmentSmartHomeBinding
import com.softylasa.smartengineer.managers.DialogManager.showFreemiumFeaturesDialog
import com.softylasa.smartengineer.data.models.Category
import com.softylasa.smartengineer.data.models.Notice
import com.softylasa.smartengineer.data.models.SemesterTotal
import com.softylasa.smartengineer.utils.helpers.ConvertFB
import com.softylasa.smartengineer.utils.helpers.Converters.formatTimestampToRelativeTime
import com.softylasa.smartengineer.ui.activities.AllBookActivity
import com.softylasa.smartengineer.ui.activities.DepartmentActivity
import com.softylasa.smartengineer.ui.activities.ProfileActivity

class SmartHomeFragment : Fragment() {

    private lateinit var binding: FragmentSmartHomeBinding
    private val tagName = "SmartHomeFragment"
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var semesterAdapter: SemesterAdapter
    private var firestoreDB: FirebaseFirestore? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseRef = FirebaseDatabase.getInstance().getReference("UsersDB/$currentUserUid/Favourites/Notice")
    private val loveCountRef = FirebaseDatabase.getInstance().getReference("UsersDB/$currentUserUid/Favourites/CountNoticeLove")

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
            semesterAdapter = SemesterAdapter(emptyList(), context,"", "")
        }

        // Enable the marquee effect
        binding.apply {
            marqueeTextView.isSelected = true // Must be true for marquee to work
            marqueeTextView.isSingleLine = true
            marqueeTextView.ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeTextView.marqueeRepeatLimit = -1 // Infinite scrolling
            fetchMarqueeNotices()
        }

        // Category or Department Set Up -----------------------------------------------------------------------------------
        binding.apply {
            activity?.let { context ->
                recycleCategory.layoutManager = GridLayoutManager(context, 3)
                recycleSemester.layoutManager = LinearLayoutManager(context)
            }

            recycleCategory.setHasFixedSize(true)
            recycleSemester.setHasFixedSize(true)
            recycleCategory.isNestedScrollingEnabled = false
            recycleSemester.isNestedScrollingEnabled = false
            fetchDepartments()
            isShimmerOn(true)
            getSemesterData()
        }

        // Button Click ----------------------------------------------------------------
        binding.apply {
            activity?.let { context ->
                btnViewProfile.setOnClickListener { startActivity(Intent(context, ProfileActivity::class.java)) }
                btnViewPremium.setOnClickListener { showFreemiumFeaturesDialog(context) }
            }


            //btnActivity.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            //btnInstitute.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            //btnRecycling.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            //btnCommunity.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            //btnCalculator.setOnClickListener { SmartData.comingSoon(requireActivity()) }
        }

        return binding.root
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
                                notice.timestamp?.let { setNoticeDialog(notice, category, it, text) }
                            }

                            val limitedText = text.split(" ").take(15).joinToString(" ") // Limit to the first 3 words
                            val clickToSeeMore = "...বিস্তারিত পড়তে ক্লিক করুন।"
                            val fullText = "$category $limitedText $clickToSeeMore" // Combine them into one string
                            val spannableString = SpannableString(fullText) // Create a SpannableString
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
    private fun setNoticeDialog(notice: Notice, category: String, timestamp: Timestamp, text: String){
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
    private fun setUpLoveButton(btnLove: AppCompatButton, notice: Notice){
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
                databaseRef.child(notice.id).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isLoved = snapshot.getValue(Boolean::class.java) ?: false
                        if (isLoved) {
                            // Remove from favorites
                            databaseRef.child(notice.id).setValue(false)
                            updateLoveButtonState(btnLove, false)

                            // Decrement loves count in Realtime Database
                            loveCountRef.child(notice.id).get().addOnSuccessListener { loveSnapshot ->
                                val currentCount = loveSnapshot.getValue(Int::class.java) ?: 0
                                loveCountRef.child(notice.id).setValue(currentCount - 1)
                            }
                        } else {
                            // Add to favorites
                            databaseRef.child(notice.id).setValue(true)
                            updateLoveButtonState(btnLove, true)

                            // Increment loves count in Realtime Database
                            loveCountRef.child(notice.id).get().addOnSuccessListener { loveSnapshot ->
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
                loveButton.setTextColor(ContextCompat.getColor(context, R.color.red)) // Set text color to red
            } else {
                loveButton.setBackgroundResource(R.drawable.abc_orange_main)
                loveButton.text = getString(R.string.love)
                loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sl_heart_black, 0, 0, 0)
                loveButton.setTextColor(ContextCompat.getColor(context, R.color.ic_light_only)) // Set text color to red
            }
        }

    }

    @SuppressLint("InflateParams")
    private fun showCustomPopupMenu(anchorView: View, notice: Notice) {
        // Inflate the custom menu layout
        val popupView = LayoutInflater.from(anchorView.context).inflate(R.layout.popup_menu_layout, null)
        // Create the PopupWindow
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true) // Focusable, so it will close when touched outside
        // Set custom background drawable (optional if background is set in XML)
        activity?.let { context ->
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.menu_background))
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
    // ===========================================       Get Category Data      ============================================
    // =====================================================================================================================
    private fun fetchDepartments() {
        val departmentList = mutableListOf<Category>()
        activity?.let { context ->
            firestoreDB?.collection("departments")
                ?.orderBy("DId", Query.Direction.ASCENDING) // Or DESCENDING for reverse order
                ?.get()?.addOnSuccessListener { querySnapshot ->
                    isShimmerOn(false)
                    for (document in querySnapshot.documents) {
                        try {
                            val department = document.toObject<Category>()
                            departmentList.add(department!!)
                            categoryAdapter = CategoryAdapter(departmentList, context)
                            binding.recycleCategory.adapter = categoryAdapter
                            // Set the click listener
                            categoryAdapter.setOnItemClickListener(object :
                                CategoryAdapter.OnItemClickListener {
                                override fun onItemClick(position: Int, dataList: Category) {
                                    val intent = Intent(context, DepartmentActivity::class.java)
                                    intent.putExtra("DCode", dataList.DCode)
                                    startActivity(intent)
                                }
                            })
                        } catch (e: IllegalStateException) {
                            Log.e(tagName, "Error getting documents: ", e)
                            isShimmerOn(true )
                        }
                    }
                }?.addOnFailureListener { e ->
                    isShimmerOn(false)
                    sToast(context, "Failed to fetch books: ${e.message}")
                }
        }

    }
    // =====================================================================================================================
    // ===========================================       Get Semester Data     =============================================
    // =====================================================================================================================
    private fun getSemesterData() {
        val semesterList = mutableListOf<SemesterTotal>()
        // Get user saved department name from Firebase Realtime database and convert it to department code
        firestoreDB?.collection("users")?.document(auth.uid!!)?.get()
            ?.addOnSuccessListener { document ->
                val departmentCode = document.getString("department")?.let {
                    ConvertFB.covertDepartmentToCode(it)
                }

                // Check user department data available or not.. If user add department then show all semester.. Other wise Show please complete your profile..
                if (departmentCode?.isEmpty() == true) {
                    binding.recycleSemester.visibility = View.GONE
                    binding.tvSemesterInfo.visibility = View.GONE
                    binding.animationView.visibility = View.VISIBLE
                    binding.viewFooter.visibility = View.VISIBLE
                    binding.tvWarningInfo.visibility = View.VISIBLE
                    binding.tvWarningInfo.text = getString(R.string.just_for_you_warn_text)

                    Log.d(tagName, "Department Code is : Not Found")
                    activity?.let { context ->
                        sToast(context, "Please complete your profile..")
                    }
                    return@addOnSuccessListener
                } else {
                    Log.d(tagName, "Department Code is : $departmentCode")
                    // If user is from 101 (BSC Engineering) or 102 (Other) department then hide CategoryRecycleView and semester information TextView..
                    when (departmentCode) {
                        "101" -> {
                            binding.recycleSemester.visibility = View.GONE
                            binding.tvSemesterInfo.visibility = View.GONE
                            binding.animationView.visibility = View.VISIBLE
                            binding.viewFooter.visibility = View.VISIBLE
                            binding.tvWarningInfo.visibility = View.VISIBLE
                            binding.tvWarningInfo.text = getString(R.string.no_data_was_found_for_the_bsc_engineering_department)
                        }
                        "102" -> {
                            binding.recycleSemester.visibility = View.GONE
                            binding.tvSemesterInfo.visibility = View.GONE
                            binding.animationView.visibility = View.VISIBLE
                            binding.viewFooter.visibility = View.VISIBLE
                            binding.tvWarningInfo.visibility = View.VISIBLE
                            binding.tvWarningInfo.text = getString(R.string.no_data_was_found_for_others_department)
                        }
                        else -> {
                            binding.recycleSemester.visibility = View.VISIBLE
                            binding.tvSemesterInfo.visibility = View.VISIBLE
                            binding.animationView.visibility = View.GONE
                            binding.viewFooter.visibility = View.GONE
                            binding.tvWarningInfo.visibility = View.GONE

                            // Fetch all semester data from Firebase FireStore Database
                            activity?.let { context ->
                                firestoreDB?.collection("semester_total")
                                    ?.get()?.addOnSuccessListener { querySnapshot ->
                                        for (documentInner in querySnapshot.documents) {
                                            val department = documentInner.toObject<SemesterTotal>()
                                            semesterList.add(department!!)

                                            // Get Users save Department all Information.. Like: Department Name, Department Abbr, Department Information, etc..
                                            departmentCode?.let { it1 ->
                                                firestoreDB?.collection("departments")
                                                    ?.document(it1)
                                                    ?.get()?.addOnSuccessListener { querySnapshotUnder ->
                                                        val userDep = querySnapshotUnder.toObject<Category>()
                                                        val dAbbr = userDep!!.DAbbr
                                                        val depName = userDep.DName

                                                        semesterAdapter = SemesterAdapter(semesterList, context, dAbbr, depName)
                                                        binding.recycleSemester.adapter = semesterAdapter

                                                        // Set the click listener
                                                        semesterAdapter.setOnItemClickListener(object :
                                                            SemesterAdapter.OnItemClickListener {
                                                            override fun onItemClick(position: Int, dAbbr: String, depName: String, semName: String) {
                                                                val intent = Intent(context, AllBookActivity::class.java)
                                                                val no = position+1

                                                                intent.putExtra("abbrNo", "${dAbbr}$no")
                                                                intent.putExtra("noAbbr", "${no}$dAbbr")
                                                                intent.putExtra("departmentName", depName)
                                                                intent.putExtra("semesterName", semName)
                                                                startActivity(intent)
                                                            }
                                                        })
                                                    }?.addOnFailureListener { lToast(context, "Failed to fetch user saved department data}") }
                                            }
                                        }
                                    }?.addOnFailureListener { lToast(context,"Failed to fetch semester data.") }
                            }

                        }
                    }
                }
            }
    }
    override fun onResume() {
        super.onResume()
        fetchDepartments()
        getSemesterData()
    }
    override fun onPause() {
        isShimmerOn(false)
        super.onPause()
    }
    private fun isShimmerOn(flag: Boolean) {
        if (flag) {
            // If data is available then Shimmer Effect will be gone
            binding.shimmerDepartmentList.startShimmer()
            binding.shimmerDepartmentList.visibility = View.VISIBLE
            binding.homeContainer.visibility = View.GONE
        } else {
            binding.shimmerDepartmentList.stopShimmer()
            binding.shimmerDepartmentList.visibility = View.GONE
            binding.homeContainer.visibility = View.VISIBLE
        }
    }

}