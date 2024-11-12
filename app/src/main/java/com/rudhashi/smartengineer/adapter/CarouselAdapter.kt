package com.rudhashi.smartengineer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.rudhashi.smartengineer.R

class CarouselAdapter(private val images: List<String>) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    inner class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val carouselImageView: AppCompatImageView = view.findViewById(R.id.carouselImageView)

        fun bind(imageUrl: String) {
            carouselImageView.load(imageUrl) {
                crossfade(true)
                transformations(RoundedCornersTransformation(8f))
                placeholder(R.drawable.slider_1) // Set a placeholder for loading images
                error(R.drawable.slider_2) // Error image if the load fails
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size
}