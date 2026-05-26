package com.softylasa.smartengineer.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.models.Features
import com.softylasa.smartengineer.databinding.ItemFeatureBinding
import com.softylasa.smartengineer.managers.DialogManager.comingSoon
import com.softylasa.smartengineer.ui.activities.AcademicBookActivity
import com.softylasa.smartengineer.ui.activities.AllDepartmentActivity
import com.softylasa.smartengineer.ui.activities.BookViewActivity
import com.softylasa.smartengineer.ui.activities.CgpaCalculatorActivity
import com.softylasa.smartengineer.ui.activities.NewsfeedActivity
import com.softylasa.smartengineer.ui.activities.NoticeBoardActivity
import com.softylasa.smartengineer.ui.activities.SearchActivity

class FeatureAdapter(
    private var dataList: ArrayList<Features>,
    private val context: Context
) : RecyclerView.Adapter<FeatureAdapter.ViewHolderClass>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val binding = ItemFeatureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        Glide.with(context)
            .load(currentItem.dataImageUrl)
            .placeholder(R.drawable.logo_orange)
            .into(holder.binding.itemImg)

        holder.binding.itemTitle.text = currentItem.dataTitle

        when (position){
            0 -> {
                holder.binding.clickItem.setBackgroundResource(R.drawable._category_shape_orange)
                holder.binding.llItemBG.setBackgroundResource(R.drawable._category_shape_orange_front)
            }
            1 -> {
                holder.binding.clickItem.setBackgroundResource(R.drawable._category_shape_blue)
                holder.binding.llItemBG.setBackgroundResource(R.drawable._category_shape_blue_front)
            }
            2 -> {
                holder.binding.clickItem.setBackgroundResource(R.drawable._category_shape_green)
                holder.binding.llItemBG.setBackgroundResource(R.drawable._category_shape_green_front)
            }
            3 -> {
                holder.binding.clickItem.setBackgroundResource(R.drawable._category_shape_green)
                holder.binding.llItemBG.setBackgroundResource(R.drawable._category_shape_green_front)
            }
            4 -> {
                holder.binding.clickItem.setBackgroundResource(R.drawable._category_shape_blue)
                holder.binding.llItemBG.setBackgroundResource(R.drawable._category_shape_blue_front)
            }
            5 -> {
                holder.binding.clickItem.setBackgroundResource(R.drawable._category_shape_orange)
                holder.binding.llItemBG.setBackgroundResource(R.drawable._category_shape_orange_front)
            }
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            when (position) {
                0 -> context.startActivity(Intent(context, NewsfeedActivity::class.java))
                1 -> context.startActivity(Intent(context, CgpaCalculatorActivity::class.java))
                2 -> context.startActivity(Intent(context, AcademicBookActivity::class.java))
                3 -> context.startActivity(Intent(context, SearchActivity::class.java))
                4 -> context.startActivity(Intent(context, AllDepartmentActivity::class.java))
                5 -> {
                    val intent = Intent(context, BookViewActivity::class.java)
                    intent.putExtra("DISPLAY", "Library")
                    context.startActivity(intent)
                }
                else -> comingSoon(context)
            }
        }

    }

    inner class ViewHolderClass(val binding: ItemFeatureBinding) : RecyclerView.ViewHolder(binding.root)
}