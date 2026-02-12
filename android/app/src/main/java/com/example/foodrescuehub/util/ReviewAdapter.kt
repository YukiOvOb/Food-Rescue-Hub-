package com.example.foodrescuehub.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.ReviewResponse

class ReviewAdapter(private var reviews: List<ReviewResponse>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUser: TextView = view.findViewById(R.id.tvReviewUser)
        val tvComment: TextView = view.findViewById(R.id.tvReviewComment)
        val ratingBar: RatingBar = view.findViewById(R.id.reviewRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.tvUser.text = review.user.name
        holder.tvComment.text = review.comment
        holder.ratingBar.rating = review.rating.toFloat()
    }

    override fun getItemCount() = reviews.size

    fun updateData(newReviews: List<ReviewResponse>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}