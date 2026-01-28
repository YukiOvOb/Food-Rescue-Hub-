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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * RecyclerView adapter for displaying listing cards
 * Uses DiffUtil for efficient list updates
 */
class ListingAdapter(
    private val onBuyClick: (Listing) -> Unit
) : ListAdapter<Listing, ListingAdapter.ListingViewHolder>(ListingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        android.util.Log.d("ListingAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing_card, parent, false)
        return ListingViewHolder(view, onBuyClick)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        android.util.Log.d("ListingAdapter", "onBindViewHolder called for position: $position")
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder for listing card
     */
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
            try {
                android.util.Log.d("ListingAdapter", "Binding listing: ${listing.title}")

                // Store name
                tvStoreName.text = listing.storeName

                // Category
                tvCategory.text = listing.category

                // Image - load first photo if available
                if (!listing.photoUrls.isNullOrEmpty()) {
                    android.util.Log.d("ListingAdapter", "Loading image: ${listing.photoUrls[0]}")
                    Glide.with(itemView.context)
                        .load(listing.photoUrls[0])
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(ivItemImage)
                } else {
                    android.util.Log.d("ListingAdapter", "No photos available")
                    ivItemImage.setImageResource(R.drawable.ic_launcher_foreground)
                }

                // Price
                tvRescuePrice.text = "$%.2f".format(listing.rescuePrice)

                // Savings label
                tvSavingsLabel.text = listing.savingsLabel

                // Pickup time - format from ISO string
                val pickupTimeText = formatPickupTime(listing.pickupStart, listing.pickupEnd)
                tvPickupTime.text = pickupTimeText

                // Time remaining
                tvTimeRemaining.text = "⏰ ${listing.timeRemaining}"

                // Buy button click
                btnBuy.setOnClickListener {
                    onBuyClick(listing)
                }

                android.util.Log.d("ListingAdapter", "Successfully bound listing: ${listing.title}")
            } catch (e: Exception) {
                android.util.Log.e("ListingAdapter", "Error binding listing: ${listing.title}", e)
            }
        }

        /**
         * Format pickup time from ISO 8601 strings
         */
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

    /**
     * DiffUtil callback for efficient list updates
     */
    class ListingDiffCallback : DiffUtil.ItemCallback<Listing>() {
        override fun areItemsTheSame(oldItem: Listing, newItem: Listing): Boolean {
            return oldItem.listingId == newItem.listingId
        }

        override fun areContentsTheSame(oldItem: Listing, newItem: Listing): Boolean {
            return oldItem == newItem
        }
    }
}
