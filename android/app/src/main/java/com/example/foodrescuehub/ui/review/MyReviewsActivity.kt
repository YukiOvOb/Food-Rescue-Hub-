package com.example.foodrescuehub.ui.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.databinding.ActivityMyReviewsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Activity to display all reviews written by the current user
 */
class MyReviewsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ORDER_ID = "order_id"  // Optional: filter by specific order
    }

    private lateinit var binding: ActivityMyReviewsBinding
    private var orderId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L).takeIf { it != -1L }

        setupToolbar()
        loadReviews()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.tvTitle.text = if (orderId != null) {
            "Order #$orderId Reviews"
        } else {
            "My Reviews"
        }
    }

    private fun loadReviews() {
        binding.progressBar.visibility = View.VISIBLE
        binding.llReviewsContainer.removeAllViews()
        binding.tvEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = if (orderId != null) {
                    RetrofitClient.apiService.getReviewsByOrder(orderId!!)
                } else {
                    RetrofitClient.apiService.getMyReviews()
                }

                if (response.isSuccessful) {
                    val reviews = response.body() ?: emptyList()
                    if (reviews.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.tvEmptyState.text = if (orderId != null) {
                            "No reviews for this order yet"
                        } else {
                            "You haven't written any reviews yet"
                        }
                    } else {
                        reviews.forEach { review ->
                            addReviewView(review)
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MyReviewsActivity,
                        "Failed to load reviews",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MyReviewsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun addReviewView(review: com.example.foodrescuehub.data.model.ListingReviewResponse) {
        val reviewView = LayoutInflater.from(this)
            .inflate(R.layout.item_review, binding.llReviewsContainer, false)

        val tvListingTitle = reviewView.findViewById<TextView>(R.id.tvListingTitle)
        val tvOrderId = reviewView.findViewById<TextView>(R.id.tvOrderId)
        val tvRating = reviewView.findViewById<TextView>(R.id.tvRating)
        val tvComment = reviewView.findViewById<TextView>(R.id.tvComment)
        val tvCreatedAt = reviewView.findViewById<TextView>(R.id.tvCreatedAt)

        tvListingTitle.text = review.listingTitle ?: "Unknown Item"
        tvOrderId.text = "Order #${review.orderId}"
        tvRating.text = "★".repeat(review.rating) + "☆".repeat(5 - review.rating)
        tvComment.text = review.comment
        tvCreatedAt.text = formatDate(review.createdAt)

        binding.llReviewsContainer.addView(reviewView)
    }

    private fun formatDate(dateString: String?): String {
        if (dateString == null) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
