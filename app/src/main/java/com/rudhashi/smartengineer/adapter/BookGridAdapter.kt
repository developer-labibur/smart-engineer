package com.rudhashi.smartengineer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.databinding.ItemBookGridBinding
import com.rudhashi.smartengineer.model.Book

class BookGridAdapter(
    private val onItemClicked: (String) -> Unit
) : ListAdapter<Book, BookGridAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) { holder.bind(getItem(position)) }
    inner class BookViewHolder(private val binding: ItemBookGridBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(book: Book) {
            binding.tvBookName.text = book.BName
            binding.tvBookCode.text = book.BCode
            binding.tvBookView.text = book.BView.toString()

            Glide.with(binding.ivBookImage.context)
                .load(book.BCover)
                .into(binding.ivBookImage)

            binding.root.setOnClickListener {
                onItemClicked(book.BCode)
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem.BId == newItem.BId
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem == newItem
    }
}