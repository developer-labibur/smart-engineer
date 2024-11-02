package com.rudhashi.smartengineer.fragment

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.CategoryAdapter
import com.rudhashi.smartengineer.databinding.FragmentTechBinding
import com.rudhashi.smartengineer.model.CategoryDataClass
import com.rudhashi.smartengineer.model.FeaturesDataClass

class TechFragment : Fragment() {

    private lateinit var binding: FragmentTechBinding
    private var tagLog = "TechFragment"

    // Variable for String Array ---------------------------------------------------------------------------------
    private lateinit var categoryDataList: ArrayList<CategoryDataClass>
    lateinit var catImageList: Array<String>
    lateinit var catTitleList: Array<String>
    lateinit var catIdList: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTechBinding.inflate(inflater, container, false)

        // Initialize category data list
        categoryDataList = ArrayList()

        // Initialize String Array for category image, title, and id
        catImageList = resources.getStringArray(R.array.arrayCategoryImageUrl)
        catTitleList = resources.getStringArray(R.array.arrayCategoryTitle)
        catIdList = resources.getStringArray(R.array.arrayCategoryId)

        // Gradient Text color Set -----------------------------------------------------------------------------------------
        binding.tvSmart.paint.shader = LinearGradient(0f,0f, binding.tvSmart.paint.measureText(binding.tvSmart.text.toString()), binding.tvSmart.textSize, intArrayOf(
            resources.getColor(R.color.gradient_start, null), resources.getColor(R.color.gradient_start, null), resources.getColor(R.color.gradient_end, null)), null, Shader.TileMode.CLAMP)
        binding.tvE.paint.shader = LinearGradient(0f,0f, binding.tvE.paint.measureText(binding.tvE.text.toString()), binding.tvE.textSize, intArrayOf(
            resources.getColor(R.color.clr2_1, null), resources.getColor(R.color.clr1_1, null), resources.getColor(R.color.clr2_1, null)), null, Shader.TileMode.CLAMP)
        binding.tvEngineer.paint.shader = LinearGradient(0f,0f, binding.tvEngineer.paint.measureText(binding.tvEngineer.text.toString()), binding.tvEngineer.textSize, intArrayOf(
            resources.getColor(R.color.clr2_1_1, null), resources.getColor(R.color.clr2_1_2, null), resources.getColor(R.color.clr2_1_3, null) ),null, Shader.TileMode.CLAMP )
        binding.tvPoly.paint.shader = LinearGradient(0f,0f, binding.tvPoly.paint.measureText(binding.tvPoly.text.toString()), binding.tvPoly.textSize, intArrayOf(
            resources.getColor(R.color.clr2_1_2, null), resources.getColor(R.color.clr2_1_1, null), resources.getColor(R.color.clr2_1_1, null) ),null, Shader.TileMode.CLAMP )

        // Adapter RecyclerView Category -----------------------------------------------------------------------------------
        binding.recycleCategory.layoutManager = GridLayoutManager(requireActivity(), 3, GridLayoutManager.VERTICAL, false)
        binding.recycleCategory.setHasFixedSize(true)
        binding.recycleCategory.isNestedScrollingEnabled = false;
        //todo: catAdapter.notifyDataSetChanged(); // Extra
        categoryDataList = arrayListOf<CategoryDataClass>()
        getCategoryData()

        return binding.root
    }

// =====================================================================================================================
// =====================================       Get Category Data                  ======================================
// =====================================================================================================================
private fun getCategoryData(){
    for (i in catImageList.indices){
        val categoryDataClass = CategoryDataClass(catIdList[i], catTitleList[i], catImageList[i])
        categoryDataList.add(categoryDataClass)
    }
    binding.recycleCategory.adapter = CategoryAdapter(categoryDataList, requireActivity())
}
}