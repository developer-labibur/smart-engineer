package com.softylasa.smartengineer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.softylasa.smartengineer.data.models.Category
import com.softylasa.smartengineer.databinding.ActivityAllDepartmentBinding
import com.softylasa.smartengineer.ui.adapters.CategoryAdapter
import com.softylasa.smartengineer.utils.helpers.GetSet.sToast

class AllDepartmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllDepartmentBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private var firestoreDB: FirebaseFirestore? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllDepartmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestoreDB = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        categoryAdapter = CategoryAdapter(emptyList(), this)
        binding.recycleCategory.layoutManager = GridLayoutManager(this, 3)
        fetchDepartments()
        isShimmerOn(true)

        binding.btnBack.setOnClickListener { finish() }

    }


    // =====================================================================================================================
    // ===========================================       Get Category Data      ============================================
    // =====================================================================================================================
    private fun fetchDepartments() {
        val departmentList = mutableListOf<Category>()
        firestoreDB?.collection("departments")
            ?.orderBy("DId", Query.Direction.ASCENDING) // Or DESCENDING for reverse order
            ?.get()?.addOnSuccessListener { querySnapshot ->
                isShimmerOn(false)
                for (document in querySnapshot.documents) {
                    try {
                        val department = document.toObject<Category>()
                        departmentList.add(department!!)
                        categoryAdapter = CategoryAdapter(departmentList, this)
                        binding.recycleCategory.adapter = categoryAdapter
                        // Set the click listener
                        categoryAdapter.setOnItemClickListener(object :
                            CategoryAdapter.OnItemClickListener {
                            override fun onItemClick(position: Int, dataList: Category) {
                                val intent = Intent(this@AllDepartmentActivity, DepartmentActivity::class.java)
                                intent.putExtra("DCode", dataList.DCode)
                                startActivity(intent)
                            }
                        })
                    } catch (e: IllegalStateException) {
                        isShimmerOn(true )
                    }
                }
            }?.addOnFailureListener { e ->
                isShimmerOn(false)
                sToast(this, "Failed to fetch books: ${e.message}")
            }
    }

    override fun onResume() {
        super.onResume()
        fetchDepartments()
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
            binding.recycleCategory.visibility = View.GONE
        } else {
            binding.shimmerDepartmentList.stopShimmer()
            binding.shimmerDepartmentList.visibility = View.GONE
            binding.recycleCategory.visibility = View.VISIBLE
        }
    }

}