package com.rudhashi.smartengineer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rudhashi.smartengineer.R
import com.rudhashi.smartengineer.model.CategoryDataClass

class SemesterAdapter (): RecyclerView.Adapter<SemesterAdapter.ViewHolderClass>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_semester_number, parent, false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return 7
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {

    }

    class ViewHolderClass (itemView: View) : RecyclerView.ViewHolder(itemView){
    }
}