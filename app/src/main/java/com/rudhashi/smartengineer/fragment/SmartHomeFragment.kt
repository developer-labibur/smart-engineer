package com.rudhashi.smartengineer.fragment

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.CategoryAdapter
import com.rudhashi.smartengineer.adapter.SemesterAdapter
import com.rudhashi.smartengineer.databinding.FragmentSmartHomeBinding
import com.rudhashi.smartengineer.model.CategoryDataClass

class SmartHomeFragment : Fragment() {

    private lateinit var binding: FragmentSmartHomeBinding
    private lateinit var categoryDataList: ArrayList<CategoryDataClass>
    private lateinit var catImageList: Array<String>
    private lateinit var catTitleList: Array<String>
    private lateinit var catIdList: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmartHomeBinding.inflate(inflater, container, false)

        // Initialize category data list and String Array for category image, title, and id --------------------------------
        categoryDataList = ArrayList()
        catImageList = resources.getStringArray(R.array.arrayCategoryImageUrl)
        catTitleList = resources.getStringArray(R.array.arrayCategoryTitle)
        catIdList = resources.getStringArray(R.array.arrayCategoryId)


        // Category or Department Set Up -----------------------------------------------------------------------------------
        binding.apply {
            recycleCategory.layoutManager = GridLayoutManager(requireActivity(), 3, GridLayoutManager.VERTICAL, false)
            recycleSemester.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            recycleCategory.setHasFixedSize(true)
            recycleSemester.setHasFixedSize(true)
            recycleCategory.isNestedScrollingEnabled = false
            recycleSemester.isNestedScrollingEnabled = false
            categoryDataList = arrayListOf<CategoryDataClass>()
            getCategoryData()
            getSemesterData()
        }

        return binding.root
    }

    // =====================================================================================================================
    // ==============================================       Get Category      ==============================================
    // =====================================================================================================================
    private fun getCategoryData() {
        for (i in catImageList.indices) {
            val categoryDataClass =
                CategoryDataClass(catIdList[i], catTitleList[i], catImageList[i])
            categoryDataList.add(categoryDataClass)
        }
        binding.recycleCategory.adapter = CategoryAdapter(categoryDataList, requireActivity())
    }
    private fun getSemesterData() {
        binding.recycleSemester.adapter = SemesterAdapter()
    }
}