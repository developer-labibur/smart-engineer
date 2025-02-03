package com.rudhashi.smartengineer.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.databinding.ItemSemesterNumberBinding
import com.rudhashi.smartengineer.data.models.SemesterTotal
import com.rudhashi.smartengineer.utils.helpers.SmartData

class SemesterAdapter (private var dataList: List<SemesterTotal>, private val context: Context, private  val dAbbr: String, private  val depName: String): RecyclerView.Adapter<SemesterAdapter.ViewHolderClass>() {
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
        val number = SmartData.randomNumber(6)
        when (number) {
            1 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_blue_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_blue_light)
                holder.binding.semView.setBackgroundResource(R.color.blue_light_color)
            }
            2 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_green_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_green_light)
                holder.binding.semView.setBackgroundResource(R.color.green_light_color)
            }
            3 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_pink_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_pink_light)
                holder.binding.semView.setBackgroundResource(R.color.pink_light_color)
            }
            4 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_orange_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_orange_light)
                holder.binding.semView.setBackgroundResource(R.color.orange_light_color)
            }
            5 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_purple_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_purple_light)
                holder.binding.semView.setBackgroundResource(R.color.purple_light_color)
            }
            6 -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_red_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_red_light)
                holder.binding.semView.setBackgroundResource(R.color.indian_red_light_color)
            }
            else -> {
                holder.binding.semImage.setBackgroundResource(R.drawable.abc_blue_main)
                holder.binding.semLayout.setBackgroundResource(R.drawable.abc_blue_light)
                holder.binding.semView.setBackgroundResource(R.color.blue_light_color)
            }
        }
    }

    class ViewHolderClass (val binding: ItemSemesterNumberBinding) : RecyclerView.ViewHolder(binding.root)
}