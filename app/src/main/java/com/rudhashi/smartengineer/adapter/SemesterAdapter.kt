package com.rudhashi.smartengineer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ItemSemesterNumberBinding
import com.rudhashi.smartengineer.model.SemeterTotal
import com.rudhashi.smartengineer.util.SmartData

class SemesterAdapter (private var dataList: List<SemeterTotal>, private val context: Context, private  val dAbbr: String, private  val depName: String): RecyclerView.Adapter<SemesterAdapter.ViewHolderClass>() {
    interface OnItemClickListener { fun onItemClick(position: Int, dAbbr: String, depName: String, semName: String) }
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) { this.listener = listener }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val binding = ItemSemesterNumberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val semesterDataList = dataList[position]

        holder.binding.semNumber.text = semesterDataList.STName
        Glide.with(context)
            .load(semesterDataList.STImage)
            .placeholder(R.drawable.logo_orange)
            .into(holder.binding.semImage)

        holder.binding.tvDep.text = depName
        holder.binding.semAbbr.text = dAbbr
        holder.itemView.setOnClickListener { listener?.onItemClick(position, dAbbr, depName, semesterDataList.STName) }

        // Random Gradient Background Color Programmatically
        val number = SmartData.randomNumber(4)
        when (number) {
            1 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_blue_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_blue_active)
                holder.binding.semView.setBackgroundResource(R.color.blue_main_color)
            }
            2 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_green_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_green_active)
                holder.binding.semView.setBackgroundResource(R.color.green_main_color)
            }
            3 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_pink_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_pink_active)
                holder.binding.semView.setBackgroundResource(R.color.pink_main_color)
            }
            4 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_orange_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_orange_active)
                holder.binding.semView.setBackgroundResource(R.color.orange_main_color)
            }
            else -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_blue_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_blue_active)
                holder.binding.semView.setBackgroundResource(R.color.blue_main_color)
            }
        }    }

    class ViewHolderClass (val binding: ItemSemesterNumberBinding) : RecyclerView.ViewHolder(binding.root)
}