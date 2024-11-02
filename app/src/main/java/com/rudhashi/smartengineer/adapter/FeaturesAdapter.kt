package com.rudhashi.smartengineer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.model.FeaturesDataClass
import com.rudhashi.smartengineer.R

class FeaturesAdapter (private val dataList: ArrayList<FeaturesDataClass>, private val context: Context): RecyclerView.Adapter<FeaturesAdapter.ViewHolderClass>() {

    // Logic Random Gradient Background Color
    private fun Int.getRandom(): Int {
        return (Math.random() * this).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_features, parent, false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        Glide.with(context)
            .load(currentItem.dataImageUrl)
            .placeholder(R.drawable.logo_orange)
            .into(holder.rvImage)

        //holder.rvImage.setImageResource(currentItem.dataImageUrl)
        holder.rvTitle.text = currentItem.dataTitle

        // Random Gradient Background Color Programmatically
        val number = 8.getRandom()
        when (number) {
            1 -> holder.bgLay.setBackgroundResource(R.drawable.gradient_1)
            2 -> holder.bgLay.setBackgroundResource(R.drawable.gradient_2)
            3 -> holder.bgLay.setBackgroundResource(R.drawable.gradient_3)
            4 -> holder.bgLay.setBackgroundResource(R.drawable.gradient_4)
            5 -> holder.bgLay.setBackgroundResource(R.drawable.gradient_5)
            6 -> holder.bgLay.setBackgroundResource(R.drawable.gradient_6)
            7 -> holder.bgLay.setBackgroundResource(R.drawable.gradient_7)
            8 -> holder.bgLay.setBackgroundResource(R.drawable.gradient_8)
            else -> holder.bgLay.setBackgroundResource(R.drawable.gradient_9)
        }
    }

    class ViewHolderClass (itemView: View) : RecyclerView.ViewHolder(itemView){
        val rvImage: ImageView = itemView.findViewById(R.id.itemImg)
        val rvTitle: TextView = itemView.findViewById(R.id.itemTitle)
        val bgLay: LinearLayout = itemView.findViewById(R.id.ll_itemBG)
    }
}