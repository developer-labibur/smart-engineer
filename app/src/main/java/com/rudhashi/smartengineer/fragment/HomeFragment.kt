package com.rudhashi.smartengineer.fragment

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.adapter.FeaturesAdapter
import com.rudhashi.smartengineer.databinding.FragmentHomeBinding
import com.rudhashi.smartengineer.model.FeaturesDataClass

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var tagLog = "HomeFragment"
    private lateinit var featuresDataList: ArrayList<FeaturesDataClass>
    //private var arrayAppFeatureItems: ArrayList<HashMap<String, String>> = ArrayList()
    //private lateinit var item: HashMap<String, String>

    // Variable for String Array ---------------------------------------------------------------------------------
    private lateinit var appFeatureId: Array<String>
    private lateinit var appFeatureTitle: Array<String>
    private lateinit var appFeatureImageUrl: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // String Array Hooks ------------------------------------------------------------------------------------
        appFeatureId = resources.getStringArray(R.array.appFeatureId)
        appFeatureTitle = resources.getStringArray(R.array.appFeatureTitle)
        appFeatureImageUrl = resources.getStringArray(R.array.appFeatureImageUrl)

        // Gradient Text color Set -----------------------------------------------------------------------------------------
        binding.tvSmart.paint.shader = LinearGradient(0f,0f, binding.tvSmart.paint.measureText(binding.tvSmart.text.toString()), binding.tvSmart.textSize, intArrayOf(
            resources.getColor(R.color.clr1, null), resources.getColor(R.color.clr2, null)), null, Shader.TileMode.CLAMP)
        binding.tvE.paint.shader = LinearGradient(0f,0f, binding.tvE.paint.measureText(binding.tvE.text.toString()), binding.tvE.textSize, intArrayOf(
            resources.getColor(R.color.clr2_1, null), resources.getColor(R.color.clr1_1, null), resources.getColor(R.color.clr2_1, null)), null, Shader.TileMode.CLAMP)
        binding.tvEngineer.paint.shader = LinearGradient(0f,0f, binding.tvEngineer.paint.measureText(binding.tvEngineer.text.toString()), binding.tvEngineer.textSize, intArrayOf(
            resources.getColor(R.color.clr2_1_1, null), resources.getColor(R.color.clr2_1_2, null), resources.getColor(R.color.clr2_1_3, null) ),null, Shader.TileMode.CLAMP )


        // Adapter RecyclerView Category -----------------------------------------------------------------------------------
        binding.recycleFeatures.layoutManager = GridLayoutManager(requireActivity(), 3, GridLayoutManager.VERTICAL, false)
        binding.recycleFeatures.setHasFixedSize(true)
        binding.recycleFeatures.isNestedScrollingEnabled = false;
        featuresDataList = arrayListOf<FeaturesDataClass>()
        getCategoryData()

        return binding.root
    }

    // =====================================================================================================================
    // =====================================       Get Category Data                  ======================================
    // =====================================================================================================================
    private fun getCategoryData(){
        for (i in appFeatureImageUrl.indices){
            val categoryDataClass = FeaturesDataClass(appFeatureId[i], appFeatureTitle[i], appFeatureImageUrl[i])
            featuresDataList.add(categoryDataClass)
        }
        binding.recycleFeatures.adapter = FeaturesAdapter(featuresDataList, requireActivity())
    }
}