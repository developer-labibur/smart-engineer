package com.rudhashi.smartengineer.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.CategoryAdapter
import com.rudhashi.smartengineer.adapter.SemesterAdapter
import com.rudhashi.smartengineer.databinding.FragmentSmartHomeBinding
import com.rudhashi.smartengineer.model.CategoryDataClass
import com.rudhashi.smartengineer.model.SemeterTotal
import com.rudhashi.smartengineer.util.ConvertFB
import com.rudhashi.smartengineer.util.SmartData
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
                                                            intent.putExtra("semesterCode", "${dAbbr}${position+1}")
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
        // If data is available then Shimmer Effect will be gone
        isShimmerOn(true)
        fetchDepartments()
        getSemesterData()
    }

    override fun onPause() {
        // Shimmer effect will be stopped when the activity is paused to save battery life.
        // If data is available then Shimmer Effect will be gone
        isShimmerOn(false)
        super.onPause()
    }

    private fun isShimmerOn(flag: Boolean) {
        /*if (flag) {
            // If data is available then Shimmer Effect will be gone
            binding.shimmerBookList.startShimmer()
            binding.shimmerBookList.visibility = View.VISIBLE
            binding.recycleCategory.visibility = View.GONE
            binding.tvJ.visibility = View.GONE
            binding.tvFullLetter.visibility = View.GONE
            binding.icJust.visibility = View.GONE
            binding.recycleSemester.visibility = View.GONE
        } else {
            binding.shimmerBookList.stopShimmer()
            binding.shimmerBookList.visibility = View.GONE
            binding.recycleCategory.visibility = View.VISIBLE
            binding.tvJ.visibility = View.VISIBLE
            binding.tvFullLetter.visibility = View.VISIBLE
            binding.icJust.visibility = View.VISIBLE
            binding.recycleSemester.visibility = View.VISIBLE
        }*/
    }

}