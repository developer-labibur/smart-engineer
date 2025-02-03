package com.rudhashi.smartengineer.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rudhashi.smartengineer.databinding.ItemChapterBinding

class ChapterAdapter(private val chapterList: List<String>, private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapterList[position]
        holder.bind(chapter)
    }

    override fun getItemCount(): Int = chapterList.size

    inner class ChapterViewHolder(private val binding: ItemChapterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chapter: String) {
            binding.chapterName.text = chapter
            binding.root.setOnClickListener {
                onClick(chapter)
            }
        }
    }
}