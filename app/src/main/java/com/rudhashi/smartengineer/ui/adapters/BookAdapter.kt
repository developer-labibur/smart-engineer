package com.rudhashi.smartengineer.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.databinding.ItemAllBookBinding
import com.rudhashi.smartengineer.data.models.Book
import com.rudhashi.smartengineer.utils.helpers.MakeFirebaseEasy

class BookAdapter(
    private val books: MutableList<Book>, // Replacing mutableList for external changes.
    private val onBookClick: (Book) -> Unit // Lambda for click listener.
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    // Inner class for ViewHolder
    inner class BookViewHolder(private val binding: ItemAllBookBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(book: Book) {
            binding.apply {
                bookTitle.text = book.BName
                bookCode.text = book.BCode
                bookViewCount.text = book.BView.toString()

                // Load book cover image with Glide
                Glide.with(bookImage.context)
                    .load(book.BCover)
                    .into(bookImage)

                // Handle book item click
                root.setOnClickListener { onBookClick(book) }

                // Set book favorites
                MakeFirebaseEasy.setBookFavorites(
                    btnFavourite, book.BCode, book.BName, book.BCover
                )
            }
        }
    }

    // Inflate ViewHolder layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemAllBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    // Update books list and refresh RecyclerView
    fun updateBooks(newBooks: List<Book>) {
        books.clear()
        books.addAll(newBooks)
        notifyDataSetChanged()
    }
}