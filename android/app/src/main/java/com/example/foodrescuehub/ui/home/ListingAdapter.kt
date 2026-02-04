package com.example.foodrescuehub.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.Listing
import com.example.foodrescuehub.util.UrlUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * RecyclerView adapter for displaying listing cards
 */
class ListingAdapter(
    private val onBuyClick: (Listing) -> Unit
) : ListAdapter<Listing, ListingAdapter.ListingViewHolder>(ListingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing_card, parent, false)
        return ListingViewHolder(view, onBuyClick)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ListingViewHolder(
        itemView: View,
        private val onBuyClick: (Listing) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvStoreName: TextView = itemView.findViewById(R.id.tvStoreName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val ivItemImage: ImageView = itemView.findViewById(R.id.ivItemImage)
        private val tvRescuePrice: TextView = itemView.findViewById(R.id.tvRescuePrice)
        private val tvSavingsLabel: TextView = itemView.findViewById(R.id.tvSavingsLabel)
        private val tvPickupTime: TextView = itemView.findViewById(R.id.tvPickupTime)
        private val tvTimeRemaining: TextView = itemView.findViewById(R.id.tvTimeRemaining)
        private val btnBuy: Button = itemView.findViewById(R.id.btnBuy)

        fun bind(listing: Listing) {
            tvStoreName.text = listing.storeName
            tvCategory.text = listing.category

            // USE UrlUtils to construct absolute image URL
            val imageUrl = if (!listing.photoUrls.isNullOrEmpty()) {
                UrlUtils.getFullUrl(listing.photoUrls[0])
            } else null

            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(ivItemImage)

            tvRescuePrice.text = "$%.2f".format(listing.rescuePrice)
            tvSavingsLabel.text = listing.savingsLabel
            tvPickupTime.text = formatPickupTime(listing.pickupStart, listing.pickupEnd)
            tvTimeRemaining.text = "⏰ ${listing.timeRemaining}"

            itemView.setOnClickListener { onBuyClick(listing) }
            btnBuy.setOnClickListener { onBuyClick(listing) }
        }

        private fun formatPickupTime(startTime: String, endTime: String): String {
            return try {
                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val start = LocalDateTime.parse(startTime, formatter)
                val end = LocalDateTime.parse(endTime, formatter)
                val timeFormatter = DateTimeFormatter.ofPattern("h:mm")
                val period = if (start.hour < 12) "AM" else "PM"
                "Pickup ${start.format(timeFormatter)}–${end.format(timeFormatter)} $period"
            } catch (e: Exception) {
                "Pickup time available"
            }
        }
    }

    class ListingDiffCallback : DiffUtil.ItemCallback<Listing>() {
        override fun areItemsTheSame(oldItem: Listing, newItem: Listing): Boolean {
            return oldItem.listingId == newItem.listingId
        }
        override fun areContentsTheSame(oldItem: Listing, newItem: Listing): Boolean {
            return oldItem == newItem
        }
    }
}
