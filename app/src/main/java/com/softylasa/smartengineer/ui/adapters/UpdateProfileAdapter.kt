package com.softylasa.smartengineer.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.softylasa.smartengineer.databinding.ItemDropdownBinding

class UpdateProfileAdapter(
    private val items: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<UpdateProfileAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemDropdownBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class CustomViewHolder(private val binding: ItemDropdownBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, onItemClick: (String) -> Unit) {
            binding.tv.text = item
            binding.itemContainer.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
