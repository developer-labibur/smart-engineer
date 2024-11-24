package com.rudhashi.smartengineer.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.rudhashi.smartengineer.databinding.ItemBookUnitBinding
import com.rudhashi.smartengineer.model.BookUnit

class UnitAdapter(
    private val context: Context,
    private val bookUnitList: List<BookUnit>
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    inner class UnitViewHolder(binding: ItemBookUnitBinding) : RecyclerView.ViewHolder(binding.root) {
        val unitName: TextView = binding.unitName
        val viewPdfButton: ConstraintLayout = binding.clickUnitItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ItemBookUnitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val unit = bookUnitList[position]

        holder.unitName.text = unit.UName

        // Open PDF link when the button is clicked
        holder.viewPdfButton.setOnClickListener {
            val pdfIntent = Intent(Intent.ACTION_VIEW, Uri.parse(unit.UPdf))
            pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(pdfIntent)
        }
    }

    override fun getItemCount(): Int = bookUnitList.size
}