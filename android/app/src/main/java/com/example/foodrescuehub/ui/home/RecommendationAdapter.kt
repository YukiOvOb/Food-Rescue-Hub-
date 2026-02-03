package com.example.foodrescuehub.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.StoreRecommendation

/**
 * Adapter for personalized store recommendations
 */
class RecommendationAdapter(
    private val onItemClick: (StoreRecommendation) -> Unit
) : ListAdapter<StoreRecommendation, RecommendationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_store_recommendation, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (StoreRecommendation) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val ivListing: ImageView = itemView.findViewById(R.id.ivListing)
        private val tvTag: TextView = itemView.findViewById(R.id.tvTag)
        private val tvStoreName: TextView = itemView.findViewById(R.id.tvStoreName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvRescuePrice: TextView = itemView.findViewById(R.id.tvRescuePrice)
        private val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)
        private val tvDiscount: TextView = itemView.findViewById(R.id.tvDiscount)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)

        fun bind(recommendation: StoreRecommendation, position: Int) {
            // Set rank badge
            tvRank.text = "#${position + 1}"

            // Load listing image
            Glide.with(itemView.context)
                .load(recommendation.photoUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .centerCrop()
                .into(ivListing)

            // Set tag with appropriate color
            val tag = recommendation.getRecommendationTag()
            tvTag.text = tag
            tvTag.setBackgroundColor(getTagColor(tag))

            // Store info
            tvStoreName.text = recommendation.storeName
            tvCategory.text = recommendation.category
            tvTitle.text = recommendation.title

            // Price info
            tvRescuePrice.text = String.format("SGD %.2f", recommendation.rescuePrice)
            tvOriginalPrice.text = String.format("~%.2f~", recommendation.originalPrice)
            tvDiscount.text = "${recommendation.savingsPercentage}% OFF"

            // Rating and distance
            tvRating.text = "★ ${recommendation.getRatingText()}"
            tvDistance.text = "📍 ${recommendation.getDistanceText()}"

            // Click listener
            itemView.setOnClickListener {
                onItemClick(recommendation)
            }
        }

        private fun getTagColor(tag: String): Int {
            return when (tag) {
                "Top Rated" -> Color.parseColor("#FF6B35")
                "Nearby" -> Color.parseColor("#4ECDC4")
                "Great Deal" -> Color.parseColor("#F7931E")
                else -> Color.parseColor("#9B9B9B")
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StoreRecommendation>() {
        override fun areItemsTheSame(
            oldItem: StoreRecommendation,
            newItem: StoreRecommendation
        ): Boolean {
            return oldItem.listingId == newItem.listingId
        }

        override fun areContentsTheSame(
            oldItem: StoreRecommendation,
            newItem: StoreRecommendation
        ): Boolean {
            return oldItem == newItem
        }
    }
}
