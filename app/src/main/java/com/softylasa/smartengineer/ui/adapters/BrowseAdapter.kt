package com.softylasa.smartengineer.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.models.Browse
import com.softylasa.smartengineer.databinding.ItemBrowseListBinding
import com.softylasa.smartengineer.utils.helpers.SmartData

class BrowseAdapter (
    private var dataList: List<Browse>, 
    private val context: Context, 
): RecyclerView.Adapter<BrowseAdapter.ViewHolderClass>() {
    
    interface OnItemClickListener { 
        fun onItemClick(position: Int, name: String) 
    }
    
    private var listener: OnItemClickListener? = null
    
    fun setOnItemClickListener(listener: OnItemClickListener) { this.listener = listener }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val binding = ItemBrowseListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val browseList = dataList[position]

        holder.binding.tvBrowseName.text = browseList.Name
        Glide.with(context)
            .load(browseList.Cover)
            .placeholder(R.drawable.logo_orange)
            .into(holder.binding.ivBrowseImage)

        holder.binding.tvBrowseInfo.text = browseList.Des
        holder.itemView.setOnClickListener { listener?.onItemClick(position, browseList.Name) }

        // Random Gradient Background Color Programmatically
        val number = SmartData.randomNumber(6)
        when (number) {
            1 -> {
                holder.binding.ivBrowseImage.setBackgroundResource(R.drawable.abc_blue_main)
                holder.binding.clickBrowseItem.setBackgroundResource(R.drawable.abc_blue_light)
            }
            2 -> {
                holder.binding.ivBrowseImage.setBackgroundResource(R.drawable.abc_green_main)
                holder.binding.clickBrowseItem.setBackgroundResource(R.drawable.abc_green_light)
            }
            3 -> {
                holder.binding.ivBrowseImage.setBackgroundResource(R.drawable.abc_pink_main)
                holder.binding.clickBrowseItem.setBackgroundResource(R.drawable.abc_pink_light)
            }
            4 -> {
                holder.binding.ivBrowseImage.setBackgroundResource(R.drawable.abc_orange_main)
                holder.binding.clickBrowseItem.setBackgroundResource(R.drawable.abc_orange_light)
            }
            5 -> {
                holder.binding.ivBrowseImage.setBackgroundResource(R.drawable.abc_purple_main)
                holder.binding.clickBrowseItem.setBackgroundResource(R.drawable.abc_purple_light)
            }
            6 -> {
                holder.binding.ivBrowseImage.setBackgroundResource(R.drawable.abc_red_main)
                holder.binding.clickBrowseItem.setBackgroundResource(R.drawable.abc_red_light)
            }
            else -> {
                holder.binding.ivBrowseImage.setBackgroundResource(R.drawable.abc_blue_main)
                holder.binding.clickBrowseItem.setBackgroundResource(R.drawable.abc_blue_light)
            }
        }
    }

    class ViewHolderClass (val binding: ItemBrowseListBinding) : RecyclerView.ViewHolder(binding.root)
}