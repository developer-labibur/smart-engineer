package com.rudhashi.smartengineer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rudhashi.smartengineer.databinding.ItemNoticeQuoteBinding
import com.rudhashi.smartengineer.model.Notice

class DynamicHeightPagerAdapter(
    private val items: List<Notice>
) : RecyclerView.Adapter<DynamicHeightPagerAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemNoticeQuoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notice: Notice) {
            binding.tvNoticeQTitle.text = notice.category
            binding.tvNoticeQText.text = notice.notice
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoticeQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

