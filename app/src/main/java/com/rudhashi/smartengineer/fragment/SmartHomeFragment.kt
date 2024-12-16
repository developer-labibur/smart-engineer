package com.rudhashi.smartengineer.fragment

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
import android.widget.Toast
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
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.CategoryAdapter
import com.rudhashi.smartengineer.adapter.SemesterAdapter
import com.rudhashi.smartengineer.databinding.DialogMarqueeNoticeBinding
import com.rudhashi.smartengineer.databinding.FragmentSmartHomeBinding
import com.rudhashi.smartengineer.model.CategoryDataClass
import com.rudhashi.smartengineer.model.Notice
import com.rudhashi.smartengineer.model.SemeterTotal
import com.rudhashi.smartengineer.util.ConvertFB
import com.rudhashi.smartengineer.util.SmartConfig
import com.rudhashi.smartengineer.util.SmartData
import com.rudhashi.smartengineer.util.formatTimestampToRelativeTime
import com.rudhashi.smartengineer.view.AllBookActivity
import com.rudhashi.smartengineer.view.DepartmentActivity
import com.rudhashi.smartengineer.view.ProfileActivity

class SmartHomeFragment : Fragment() {

    private lateinit var binding: FragmentSmartHomeBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var semesterAdapter: SemesterAdapter
    private var db: FirebaseFirestore? = null
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
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        categoryAdapter = CategoryAdapter(emptyList(), requireActivity())
        semesterAdapter = SemesterAdapter(emptyList(), requireActivity(),"", "")

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
            recycleCategory.layoutManager = GridLayoutManager(requireActivity(), 3)
            recycleSemester.layoutManager = LinearLayoutManager(requireActivity())
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
            btnViewProfile.setOnClickListener { startActivity(Intent(requireActivity(), ProfileActivity::class.java)) }
            btnViewPremium.setOnClickListener { SmartData.comingSoon(requireActivity()) }

            btnActivity.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            btnInstitute.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            btnRecycling.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            btnCommunity.setOnClickListener { SmartData.comingSoon(requireActivity()) }
            btnCalculator.setOnClickListener { SmartData.comingSoon(requireActivity()) }
        }

        return binding.root
    }


    private fun fetchMarqueeNotices() {
        db?.collection("polytechnic_notice")
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
        val builder = AlertDialog.Builder(requireActivity()).create()
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
                            deductPoints(SmartConfig.UNLIKE_DEDUCT_POINT)
                        } else {
                            // Add to favorites
                            databaseRef.child(notice.id).setValue(true)
                            updateLoveButtonState(btnLove, true)

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
    private fun updateLoveButtonState(loveButton: AppCompatButton, isLoved: Boolean) {
        if (isLoved) {
            loveButton.setBackgroundResource(R.drawable.bg_notice_item_loved)
            loveButton.text = getString(R.string.loved)
            loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sl_heart_red, 0, 0, 0)
            loveButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red)) // Set text color to red
        } else {
            loveButton.setBackgroundResource(R.drawable.abc_orange_main)
            loveButton.text = getString(R.string.love)
            loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sl_heart_black, 0, 0, 0)
            loveButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.ic_light_only)) // Set text color to red
        }
    }

    @SuppressLint("InflateParams")
    private fun showCustomPopupMenu(anchorView: View, notice: Notice) {
        // Inflate the custom menu layout
        val popupView = LayoutInflater.from(anchorView.context).inflate(R.layout.popup_menu_layout, null)
        // Create the PopupWindow
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true) // Focusable, so it will close when touched outside
        // Set custom background drawable (optional if background is set in XML)
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.menu_background))
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
    @Suppress("SameParameterValue")
    private fun addPoints(pointsToAdd: Int) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = Firebase.database
        val userRef = database.getReference("Users/$currentUserUid/points")
        userRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.value?.toString()?.toInt() ?: 0
            val newPoints = currentPoints + pointsToAdd
            userRef.setValue(newPoints)
        }
    }
    @Suppress("SameParameterValue")
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
    // =====================================================================================================================
    // ===========================================       Get Category Data      ============================================
    // =====================================================================================================================
    private fun fetchDepartments() {

        val departmentList = mutableListOf<CategoryDataClass>()

        db?.collection("departments")
            ?.orderBy("DId", Query.Direction.ASCENDING) // Or DESCENDING for reverse order
            ?.get()?.addOnSuccessListener { querySnapshot ->
                isShimmerOn(false)
                for (document in querySnapshot.documents) {
                    try {
                        val department = document.toObject<CategoryDataClass>()
                        departmentList.add(department!!)
                        categoryAdapter = CategoryAdapter(departmentList, requireActivity())
                        binding.recycleCategory.adapter = categoryAdapter
                        // Set the click listener
                        categoryAdapter.setOnItemClickListener(object :
                            CategoryAdapter.OnItemClickListener {
                            override fun onItemClick(position: Int, dataList: CategoryDataClass) {
                                val intent = Intent(requireActivity(), DepartmentActivity::class.java)
                                intent.putExtra("DAbbr", dataList.DAbbr)
                                intent.putExtra("DCode", dataList.DCode)
                                intent.putExtra("DCover", dataList.DCover)
                                intent.putExtra("DInfo", dataList.DInfo)
                                intent.putExtra("DName", dataList.DName)
                                startActivity(intent)
                            }
                        })
                    } catch (e: IllegalStateException) {
                        Log.e("TEST_SL", "Error getting documents: ", e)

                        // TODO : AI khane set korbo je -> Department a Loading effect dekhabe
                    }

                }
            }?.addOnFailureListener { e ->
                isShimmerOn(false)
                Toast.makeText(
                    requireActivity(),
                    "Failed to fetch books: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    // =====================================================================================================================
    // ===========================================       Get Semester Data     =============================================
    // =====================================================================================================================
    private fun getSemesterData() {
        val semesterList = mutableListOf<SemeterTotal>()

        // Get user saved department name from Firebase Realtime database and convert it to department code
        database.reference.child("Users").child(auth.uid!!).child("department").get()
            .addOnSuccessListener {
                val departmentCode = it.value?.let { it1 -> ConvertFB.covertDepartmentToCode(it1) }

                // Check user department data available or not.. If user add department then show all semester.. Other wise Show please complete your profile..
                if (departmentCode?.isEmpty() == true) {
                    binding.recycleSemester.visibility = View.GONE
                    binding.tvSemesterInfo.visibility = View.GONE
                    binding.animationView.visibility = View.VISIBLE
                    binding.viewFooter.visibility = View.VISIBLE
                    binding.tvWarningInfo.visibility = View.VISIBLE
                    binding.tvWarningInfo.text = getString(R.string.just_for_you_warn_text)

                    Log.d("getSemesterData", "Department Code is : Not Found")
                    SmartData.lToast(requireActivity(), "Please complete your profile..")
                    return@addOnSuccessListener
                } else {
                    Log.d("getSemesterData", "Department Code is : $departmentCode")

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
                            db?.collection("semester_total")
                                ?.get()?.addOnSuccessListener { querySnapshot ->
                                    for (document in querySnapshot.documents) {
                                        val department = document.toObject<SemeterTotal>()
                                        semesterList.add(department!!)

                                        // Get Users save Department all Information.. Like: Department Name, Department Abbr, Department Information, etc..
                                        departmentCode?.let { it1 ->
                                            db?.collection("departments")
                                                ?.document(it1)
                                                ?.get()?.addOnSuccessListener { querySnapshotUnder ->
                                                    val userDep = querySnapshotUnder.toObject<CategoryDataClass>()
                                                    val dAbbr = userDep!!.DAbbr
                                                    val depName = userDep.DName

                                                    semesterAdapter = SemesterAdapter(semesterList, requireActivity(), dAbbr, depName)
                                                    binding.recycleSemester.adapter = semesterAdapter

                                                    // Set the click listener
                                                    semesterAdapter.setOnItemClickListener(object :
                                                        SemesterAdapter.OnItemClickListener {
                                                        override fun onItemClick(position: Int, dAbbr: String, depName: String, semName: String) {
                                                            val intent = Intent(requireActivity(), AllBookActivity::class.java)
                                                            intent.putExtra("fieldName", "xSem")
                                                            intent.putExtra("departmentAbbr", "${dAbbr}${position+1}")
                                                            intent.putExtra("abbr", dAbbr)
                                                            intent.putExtra("no", position+1)
                                                            intent.putExtra("semesterNo", position+1)
                                                            intent.putExtra("departmentName", depName)
                                                            intent.putExtra("semesterName", semName)
                                                            startActivity(intent)
                                                        }
                                                    })
                                                }?.addOnFailureListener { SmartData.lToast(requireActivity(), "Failed to fetch user saved department data}") }
                                        }
                                    }
                                }?.addOnFailureListener { SmartData.lToast(requireActivity(),"Failed to fetch semester data.") }
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