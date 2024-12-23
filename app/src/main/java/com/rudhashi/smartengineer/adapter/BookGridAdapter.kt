package com.rudhashi.smartengineer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ItemBookGridBinding
import com.rudhashi.smartengineer.model.Book
import com.rudhashi.smartengineer.util.MakeFirebaseEasy
import com.rudhashi.smartengineer.util.SmartData
import kotlin.properties.Delegates

class BookGridAdapter(
    private val onItemClicked: (String) -> Unit
) : RecyclerView.Adapter<BookGridAdapter.BookViewHolder>() {

    private var bookList: List<Book> = emptyList()
    private var isRandom by Delegates.notNull<Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(bookList[position])
    }

    override fun getItemCount(): Int = bookList.size

    // Method to update the data list
    @SuppressLint("NotifyDataSetChanged")
    fun setData(newBookList: List<Book>, isRandom: Boolean) {
        bookList = newBookList
        this.isRandom = isRandom
        notifyDataSetChanged() // Notify the adapter of data change
    }

    inner class BookViewHolder(private val binding: ItemBookGridBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(book: Book) {
            binding.tvBookName.text = book.BName
            binding.tvBookCode.text = book.BCode
            binding.tvBookView.text = book.BView.toString()

            Glide.with(binding.ivBookImage.context)
                .load(book.BCover)
                .into(binding.ivBookImage)

            // Called Set book Favourites Function and Add Favourites Button functionality
            MakeFirebaseEasy.setBookFavorites(binding.btnFavourite, book.BCode, book.BName, book.BCover)

            // Random Gradient Background Color Programmatically
            val number = SmartData.randomNumber(6)
            if (isRandom){
                when (number) {
                    1 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_blue_main)
                        binding.btnReadNow.setBackgroundResource(R.drawable.abc_blue_main)
                        binding.clickBookItem.setBackgroundResource(R.drawable.abc_blue_light)
                    }
                    2 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_green_main)
                        binding.btnReadNow.setBackgroundResource(R.drawable.abc_green_main)
                        binding.clickBookItem.setBackgroundResource(R.drawable.abc_green_light)
                    }
                    3 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_pink_main)
                        binding.btnReadNow.setBackgroundResource(R.drawable.abc_pink_main)
                        binding.clickBookItem.setBackgroundResource(R.drawable.abc_pink_light)
                    }
                    4 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_orange_main)
                        binding.btnReadNow.setBackgroundResource(R.drawable.abc_orange_main)
                        binding.clickBookItem.setBackgroundResource(R.drawable.abc_orange_light)
                    }
                    5 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_purple_main)
                        binding.btnReadNow.setBackgroundResource(R.drawable.abc_purple_main)
                        binding.clickBookItem.setBackgroundResource(R.drawable.abc_purple_light)
                    }
                    6 -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_red_main)
                        binding.btnReadNow.setBackgroundResource(R.drawable.abc_red_main)
                        binding.clickBookItem.setBackgroundResource(R.drawable.abc_red_light)
                    }
                    else -> {
                        binding.llItemBG.setBackgroundResource(R.drawable.abc_blue_main)
                        binding.btnReadNow.setBackgroundResource(R.drawable.abc_blue_main)
                        binding.clickBookItem.setBackgroundResource(R.drawable.abc_blue_light)
                    }
                }
            }

            binding.root.setOnClickListener {
                onItemClicked(book.BCode)
            }
        }
    }
}