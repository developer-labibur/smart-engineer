package com.rudhashi.smartengineer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.databinding.ItemAllBookBinding
import com.rudhashi.smartengineer.model.Book

class BookAdapter(
    private val onItemClicked: (String) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemAllBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) { holder.bind(getItem(position)) }
    inner class BookViewHolder(private val binding: ItemAllBookBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(book: Book) {
            binding.bookTitle.text = book.BName
            binding.bookCode.text = book.BCode
            binding.bookViewCount.text = book.BView.toString()

            Glide.with(binding.bookImage.context)
                .load(book.BCover)
                .into(binding.bookImage)

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