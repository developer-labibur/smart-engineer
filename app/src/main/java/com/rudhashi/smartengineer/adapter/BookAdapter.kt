package com.rudhashi.smartengineer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ItemAllBookBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.util.MakeFirebaseEasy
import com.rudhashi.smartengineer.util.SmartData

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

            // Called Set book Favourites Function and Add Favourites Button functionality
            MakeFirebaseEasy.setBookFavorites(binding.btnFavourite, book.BCode, book.BName, book.BCover)

            // Random Gradient Background Color Programmatically
            val number = SmartData.randomNumber(6)
            when (number) {
                1 -> {
                    binding.llItemBG.setBackgroundResource(R.drawable.abc_blue_main)
                    binding.btnRead.setBackgroundResource(R.drawable.abc_blue_main)
                }
                2 -> {
                    binding.llItemBG.setBackgroundResource(R.drawable.abc_green_main)
                    binding.btnRead.setBackgroundResource(R.drawable.abc_green_main)
                }
                3 -> {
                    binding.llItemBG.setBackgroundResource(R.drawable.abc_pink_main)
                    binding.btnRead.setBackgroundResource(R.drawable.abc_pink_main)
                }
                4 -> {
                    binding.llItemBG.setBackgroundResource(R.drawable.abc_orange_main)
                    binding.btnRead.setBackgroundResource(R.drawable.abc_orange_main)
                }
                5 -> {
                    binding.llItemBG.setBackgroundResource(R.drawable.abc_red_main)
                    binding.btnRead.setBackgroundResource(R.drawable.abc_red_main)
                }
                6 -> {
                    binding.llItemBG.setBackgroundResource(R.drawable.abc_purple_main)
                    binding.btnRead.setBackgroundResource(R.drawable.abc_purple_main)
                }
                else -> {
                    binding.llItemBG.setBackgroundResource(R.drawable.abc_blue_main)
                    binding.btnRead.setBackgroundResource(R.drawable.abc_blue_main)
                }
            }
        }
    }


    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem.BId == newItem.BId
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem == newItem
    }
}