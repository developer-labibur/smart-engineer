package com.softylasa.smartengineer.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.data.models.Links
import com.softylasa.smartengineer.databinding.ItemLinkListBinding
import com.softylasa.smartengineer.utils.helpers.Converters.dpToPx

class LinkAdapter(
    private var dataList: List<Links>,
    private val context: Context
) : RecyclerView.Adapter<LinkAdapter.ViewHolderClass>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, link: String)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val binding = ItemLinkListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val link = dataList[position]

        Glide.with(context)
            .load(link.Cover)
            .transform(RoundedCorners(dpToPx(context, 10f)))
            .placeholder(R.drawable.logo_orange)
            .into(holder.binding.ivLinkImage)
        
        holder.binding.tvLinkName.text = link.Name
        holder.binding.tvLinkInfo.text = link.Des
        
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position, link.Link)
        }
    }

    class ViewHolderClass(val binding: ItemLinkListBinding) : RecyclerView.ViewHolder(binding.root)
}