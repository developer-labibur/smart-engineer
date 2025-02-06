package com.softylasa.smartengineer.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.models.Book
import com.softylasa.smartengineer.data.models.Category
import com.softylasa.smartengineer.data.models.Favorite
import com.softylasa.smartengineer.data.models.SemesterTotal
import com.softylasa.smartengineer.databinding.FragmentSmartStudyBinding
import com.softylasa.smartengineer.managers.DialogManager.showFreemiumFeaturesDialog
import com.softylasa.smartengineer.ui.activities.AllBookActivity
import com.softylasa.smartengineer.ui.activities.BookViewActivity
import com.softylasa.smartengineer.ui.activities.DepartmentActivity
import com.softylasa.smartengineer.ui.activities.ProfileActivity
import com.softylasa.smartengineer.ui.activities.SearchActivity
import com.softylasa.smartengineer.ui.adapters.CategoryAdapter
import com.softylasa.smartengineer.ui.adapters.SemesterAdapter
import com.softylasa.smartengineer.utils.helpers.ConvertFB
import com.softylasa.smartengineer.utils.helpers.Converters.covertDepartmentToAbbr
import com.softylasa.smartengineer.utils.helpers.Converters.covertSemNameToCode
import com.softylasa.smartengineer.utils.helpers.GetSet.lToast
import com.softylasa.smartengineer.utils.helpers.GetSet.sToast

class SmartStudyFragment : Fragment() {

    private lateinit var binding: FragmentSmartStudyBinding
    private val tagName = "SmartHomeFragment"
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var semesterAdapter: SemesterAdapter
    private var firestoreDB: FirebaseFirestore? = null
    private val dbRealtime = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val favoriteBooks = mutableListOf<Book>()
    private val booksList = mutableListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmartStudyBinding.inflate(inflater, container, false)
        firestoreDB = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        activity?.let { context ->
            categoryAdapter = CategoryAdapter(emptyList(), context)
            semesterAdapter = SemesterAdapter(emptyList(), context,"", "")
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

                searchBox.setOnClickListener { startActivity(Intent(context, SearchActivity::class.java)) }

                myLibrary.setOnClickListener {
                    val intent = Intent(context, BookViewActivity::class.java)
                    intent.putExtra("DISPLAY", "Library")
                    startActivity(intent)
                }
                myFavourite.setOnClickListener {
                    val intent = Intent(context, BookViewActivity::class.java)
                    intent.putExtra("DISPLAY", "Favourite")
                    startActivity(intent) }
            }
        }

        getLibraryTotalBooksNumber()
        getFavoriteTotalBooksNumber()

        return binding.root
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

    // =====================================================================================================================
    // ============================       Get Number of Books for Library and Favorite     =================================
    // =====================================================================================================================
    private fun getLibraryTotalBooksNumber(){
        val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUID != null) {
            val userRef = firestoreDB?.collection("users")?.document(currentUserUID)
            userRef?.get()?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val department = documentSnapshot.getString("department")
                    val semester = documentSnapshot.getString("semester")

                    if (!department.isNullOrEmpty() && !semester.isNullOrEmpty()) {
                        // Convert department and semester to codes
                        val departmentAbbr = covertDepartmentToAbbr(department)
                        val semesterCode = covertSemNameToCode(semester)
                        val xSem = "$departmentAbbr$semesterCode"

                        val booksRef = firestoreDB?.collection("books")
                        booksRef?.whereArrayContains("xSem", xSem)?.get()
                            ?.addOnSuccessListener { querySnapshot ->
                                booksList.clear() // Clear previous data
                                for (document in querySnapshot) {
                                    val book = document.toObject(Book::class.java)
                                    booksList.add(book)
                                    if (isAdded) {
                                        val libTotal = context?.getString(R.string.total___books, booksList.size.toString()) ?: "Total 0 books"
                                        binding.tvLibNo.text = libTotal
                                    } else {
                                        // Fragment is not attached, handle it safely
                                    }

                                }
                            }
                            ?.addOnFailureListener { exception ->
                                binding.tvLibNo.text = getString(R.string.total___books, "0")
                                Log.e("FirestoreError", "Error fetching books: ${exception.message}")
                            }
                    } else {
                        binding.tvLibNo.text = getString(R.string.total___books, "0")
                        Log.e("UserDataError", "Department or Semester is missing for the user.")
                    }
                } else {
                    binding.tvLibNo.text = getString(R.string.total___books, "0")
                    Log.e("FirestoreError", "User document does not exist.")
                }
            }?.addOnFailureListener { exception ->
                binding.tvLibNo.text = getString(R.string.total___books, "0")
                Log.e("FirestoreError", "Error fetching user data: ${exception.message}")
            }
        }
    }

    private fun getFavoriteTotalBooksNumber(){
        val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUID != null) {
            val favoritesRef = dbRealtime.child("UsersDB").child(currentUserUID).child("Favourites").child("Books")
            favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        favoriteBooks.clear() // Clear previous data
                        // Iterate through all the BCode in the favorites list
                        for (favoriteSnapshot in snapshot.children) {
                            val favorite = favoriteSnapshot.getValue(Favorite::class.java)

                            val bookRef = favorite?.let { firestoreDB?.collection("books")?.document(it.BCode) }
                            bookRef?.get()?.addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val book = documentSnapshot.toObject(Book::class.java)
                                    book?.let {
                                        // Add book to the favoriteBooks list
                                        favoriteBooks.add(it)
                                        if (isAdded) {
                                            val libTotal = context?.getString(R.string.total___books, favoriteBooks.size.toString()) ?: "Total 0 books"
                                            binding.tvDep.text = libTotal
                                        } else {
                                            // Fragment is not attached, handle it safely
                                        }
                                    }
                                }
                            }?.addOnFailureListener { exception ->
                                Log.e("FirebaseError", "Error fetching book details: ${exception.message}")
                                binding.tvDep.text = getString(R.string.total___books, "0")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors if any
                    Log.e("FirebaseError", "Error fetching favorites: ${error.message}")
                    binding.tvDep.text = getString(R.string.total___books, "0")
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDepartments()
        getSemesterData()
        getLibraryTotalBooksNumber()
        getFavoriteTotalBooksNumber()
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

    private var safeContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onDetach() {
        super.onDetach()
        safeContext = null
    }

}