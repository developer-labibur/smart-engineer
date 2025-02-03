package com.softylasa.smartengineer.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.ItemFeaturesBinding
import com.softylasa.smartengineer.data.models.Category

class CategoryAdapter (private var dataList: List<Category>, private val context: Context): RecyclerView.Adapter<CategoryAdapter.ViewHolderClass>() {

    // Define the interface for item click
    interface OnItemClickListener {
        fun onItemClick(position: Int, dataList: Category)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    // Logic Random Gradient Background Color
    private fun Int.getRandom(): Int { return (Math.random() * this).toInt() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val binding = ItemFeaturesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(binding)
    }
    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        Glide.with(context)
            .load(currentItem.DIcon)
            .placeholder(R.drawable.logo_orange)
            .into(holder.binding.itemImg)

        holder.binding.itemTitle.text = currentItem.DName

        // Random Gradient Background Color Programmatically
        val number = 8.getRandom()
        when (number) {
            1 -> holder.binding.llItemBG.setBackgroundResource(R.drawable.gradient_1)
            2 -> holder.binding.llItemBG.setBackgroundResource(R.drawable.gradient_2)
            3 -> holder.binding.llItemBG.setBackgroundResource(R.drawable.gradient_3)
            4 -> holder.binding.llItemBG.setBackgroundResource(R.drawable.gradient_4)
            5 -> holder.binding.llItemBG.setBackgroundResource(R.drawable.gradient_5)
            6 -> holder.binding.llItemBG.setBackgroundResource(R.drawable.gradient_6)
            7 -> holder.binding.llItemBG.setBackgroundResource(R.drawable.gradient_7)
            8 -> holder.binding.llItemBG.setBackgroundResource(R.drawable.gradient_8)
            else -> holder.binding.llItemBG.setBackgroundResource(R.drawable.gradient_9)
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position, currentItem)
        }

    }
    class ViewHolderClass(val binding: ItemFeaturesBinding) : RecyclerView.ViewHolder(binding.root)
}