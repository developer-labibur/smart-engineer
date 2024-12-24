package com.rudhashi.smartengineer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class TableOfContentAdapter(
    private val context: Context,
    private val unitList: List<TableOfContentItem>,
    private val onItemClick: (TableOfContentItem, String) -> Unit
) : RecyclerView.Adapter<TableOfContentAdapter.BookUnitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookUnitViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_toc, parent, false)
        return BookUnitViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookUnitViewHolder, position: Int) {
        val bookUnit = unitList[position]
        val cachedFile = File(context.cacheDir, "${bookUnit.title}.pdf")

        holder.unitName.text = bookUnit.title
        holder.downloadImage.visibility = if (cachedFile.exists()) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener { onItemClick(bookUnit, "view") }
        holder.downloadImage.setOnClickListener { onItemClick(bookUnit, "download") }
    }

    override fun getItemCount(): Int = unitList.size

    class BookUnitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val unitName: TextView = view.findViewById(R.id.title)
        val downloadImage: ImageView = view.findViewById(R.id.ivImage)
    }
}