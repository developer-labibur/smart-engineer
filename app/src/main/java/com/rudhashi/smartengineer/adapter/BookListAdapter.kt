package com.rudhashi.smartengineer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ItemBookGridBinding
import com.rudhashi.smartengineer.databinding.ItemBookListBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.util.SmartData

class BookListAdapter(
    private val onItemClicked: (String) -> Unit
) : ListAdapter<Book, BookListAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) { holder.bind(getItem(position)) }
    inner class BookViewHolder(private val binding: ItemBookListBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(book: Book) {
            binding.tvBookName.text = book.BName
            binding.tvBookCode.text = book.BCode
            binding.tvBookView.text = book.BView.toString()

            Glide.with(binding.ivBookImage.context)
                .load(book.BCover)
                .into(binding.ivBookImage)

            // Random Gradient Background Color Programmatically
            val number = SmartData.randomNumber(6)
            when (number) {
                1 -> {
                    binding.ivBookImage.setBackgroundResource(R.drawable.abc_blue_main)
                    binding.btnReadNow.setBackgroundResource(R.drawable.abc_blue_main)
                    binding.clickBookItem.setBackgroundResource(R.drawable.abc_blue_light)
                    binding.bookView.setBackgroundResource(R.drawable.abc_blue_light)
                }
                2 -> {
                    binding.ivBookImage.setBackgroundResource(R.drawable.abc_green_main)
                    binding.btnReadNow.setBackgroundResource(R.drawable.abc_green_main)
                    binding.clickBookItem.setBackgroundResource(R.drawable.abc_green_light)
                    binding.bookView.setBackgroundResource(R.drawable.abc_green_light)
                }
                3 -> {
                    binding.ivBookImage.setBackgroundResource(R.drawable.abc_pink_main)
                    binding.btnReadNow.setBackgroundResource(R.drawable.abc_pink_main)
                    binding.clickBookItem.setBackgroundResource(R.drawable.abc_pink_light)
                    binding.bookView.setBackgroundResource(R.drawable.abc_pink_light)
                }
                4 -> {
                    binding.ivBookImage.setBackgroundResource(R.drawable.abc_orange_main)
                    binding.btnReadNow.setBackgroundResource(R.drawable.abc_orange_main)
                    binding.clickBookItem.setBackgroundResource(R.drawable.abc_orange_light)
                    binding.bookView.setBackgroundResource(R.drawable.abc_orange_light)
                }
                5 -> {
                    binding.ivBookImage.setBackgroundResource(R.drawable.abc_purple_main)
                    binding.btnReadNow.setBackgroundResource(R.drawable.abc_purple_main)
                    binding.clickBookItem.setBackgroundResource(R.drawable.abc_purple_light)
                    binding.bookView.setBackgroundResource(R.drawable.abc_purple_light)
                }
                6 -> {
                    binding.ivBookImage.setBackgroundResource(R.drawable.abc_red_main)
                    binding.btnReadNow.setBackgroundResource(R.drawable.abc_red_main)
                    binding.clickBookItem.setBackgroundResource(R.drawable.abc_red_light)
                    binding.bookView.setBackgroundResource(R.drawable.abc_red_light)
                }
                else -> {
                    binding.ivBookImage.setBackgroundResource(R.drawable.abc_blue_main)
                    binding.btnReadNow.setBackgroundResource(R.drawable.abc_blue_main)
                    binding.clickBookItem.setBackgroundResource(R.drawable.abc_blue_light)
                    binding.bookView.setBackgroundResource(R.drawable.abc_blue_light)
                }
            }

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