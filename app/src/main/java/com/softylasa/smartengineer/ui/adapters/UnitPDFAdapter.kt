package com.softylasa.smartengineer.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.databinding.ItemUnitBinding
import com.softylasa.smartengineer.data.models.BookUnit

class UnitPDFAdapter(
    private val selectedUnit: String,
    private val onItemClick: (BookUnit) -> Unit
) : RecyclerView.Adapter<UnitPDFAdapter.UnitViewHolder>() {

    private val units = mutableListOf<BookUnit>()

    fun submitList(newUnits: List<BookUnit>) {
        units.clear()
        units.addAll(newUnits)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ItemUnitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        holder.bind(units[position], units[position].UName == selectedUnit)
    }

    override fun getItemCount(): Int = units.size

    inner class UnitViewHolder(private val binding: ItemUnitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(unit: BookUnit, isSelected: Boolean) {
            binding.apply {
                unitName.text = unit.UName
                unitName.setTextColor(
                    if (isSelected) unitName.context.getColor(R.color.mainContainer_SelectedText)
                    else unitName.context.getColor(R.color.mainContainer_HeadingText)
                )
                root.setOnClickListener { onItemClick(unit) }
            }
        }
    }
}