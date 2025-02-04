package com.softylasa.smartengineer.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.ActivityUpdateProfileBinding
import com.softylasa.smartengineer.ui.adapters.UpdateProfileAdapter

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView

    private var selectedDepartment: String? = null
    private var selectedSemester: String? = null
    private var selectedInstitute: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()

        // Initialize Views
        recyclerView = binding.recyclerView

        binding.btnBack.setOnClickListener { finish() }

        // Step 1: Show Departments
        loadDepartments()

        isShimmerOn(true)
    }

    private fun loadDepartments() {
        binding.tvTitle.text = getString(R.string.select_department)
        binding.tvSubTitle.text = getString(R.string.sub_title_select_department)
        binding.searchBoxLayout.visibility = View.GONE
        binding.btnAddInstitute.visibility = View.GONE

        firestore.collection("departments")
            .orderBy("DCode", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                isShimmerOn(false)

                val departments = result.map { it.getString("DName") ?: "" }
                setupRecyclerView(departments) { department ->

                    selectedDepartment = department
                    if (department.contains("BSC Engineering")){
                        selectedSemester = ""
                        loadInstitutes()
                    }else if (department.contains("Other")){
                        selectedSemester = ""
                        selectedInstitute = ""
                        saveToFirebase()
                    }else{
                        loadSemesters()
                        isShimmerOn(true)
                    }
                }
            }.addOnFailureListener { isShimmerOn(false) }
    }

    private fun loadSemesters() {
        binding.tvTitle.text = getString(R.string.select_semester)
        binding.tvSubTitle.text = getString(R.string.sub_title_select_semester)
        binding.searchBoxLayout.visibility = View.GONE
        binding.btnAddInstitute.visibility = View.GONE

        firestore.collection("semester_total")
            .get()
            .addOnSuccessListener { result ->
                isShimmerOn(false)

                val semesters = result.map { it.getString("STName") ?: "" }
                setupRecyclerView(semesters) { semester ->
                    selectedSemester = semester
                    loadInstitutes()
                    isShimmerOn(true)
                }
            }.addOnFailureListener { isShimmerOn(false) }
    }

    private fun loadInstitutes() {
        binding.tvTitle.text = getString(R.string.select_institute)
        binding.tvSubTitle.text = getString(R.string.sub_title_select_institute)
        binding.searchBoxLayout.visibility = View.VISIBLE
        binding.btnAddInstitute.visibility = View.VISIBLE

        firestore.collection("institutes")
            .get()
            .addOnSuccessListener { result ->
                isShimmerOn(false)

                val institutes = result.map { it.getString("name") ?: "" }
                setupRecyclerView(institutes) { institute ->
                    selectedInstitute = institute
                    saveToFirebase()
                }

                // Add Search Functionality
                binding.searchBox.addTextChangedListener { text ->
                    val filtered = institutes.filter { it.contains(text.toString(), true) }
                    setupRecyclerView(filtered) { institute ->
                        selectedInstitute = institute
                        saveToFirebase()
                    }
                }
            }.addOnFailureListener { isShimmerOn(false) }

        // Handle Add Institute
        binding.btnAddInstitute.setOnClickListener {
            val newInstitute = binding.searchBox.text.toString().trim()
            if (newInstitute.isNotEmpty()) {
                selectedInstitute = newInstitute
                saveToFirebase()
            } else {
                binding.searchBox.error = "Please enter your Institute name"
            }
        }
    }

    private fun saveToFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userData = mapOf(
            "department" to selectedDepartment,
            "semester" to selectedSemester,
            "institute" to selectedInstitute
        )
        firestore.collection("users").document(uid)
            .update(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                finish() // Navigate back to ProfileActivity
            }
    }

    private fun setupRecyclerView(items: List<String>, onItemClick: (String) -> Unit) {
        recyclerView.adapter = UpdateProfileAdapter(items, onItemClick)
        recyclerView.layoutManager = LinearLayoutManager(this)
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
            binding.container.visibility = View.GONE
        } else {
            binding.shimmerDepartmentList.stopShimmer()
            binding.shimmerDepartmentList.visibility = View.GONE
            binding.container.visibility = View.VISIBLE
        }
    }
}