package com.example.foodrescuehub.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.BannerItem

/**
 * Adapter for banner carousel
 */
class BannerAdapter(
    private val bannerItems: List<BannerItem>
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(bannerItems[position])
    }

    override fun getItemCount(): Int = bannerItems.size

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBanner: ImageView = itemView.findViewById(R.id.ivBanner)
        private val tvBannerTitle: TextView = itemView.findViewById(R.id.tvBannerTitle)

        fun bind(item: BannerItem) {
            tvBannerTitle.text = item.title

            // Load banner image using Glide
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .centerCrop()
                .into(ivBanner)
        }
    }
}
