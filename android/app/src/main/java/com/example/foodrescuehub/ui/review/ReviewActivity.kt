package com.example.foodrescuehub.ui.review

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.ReviewRequest
import com.example.foodrescuehub.databinding.ActivityReviewBinding
import kotlinx.coroutines.launch

class ReviewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
        const val EXTRA_LISTING_ID = "extra_listing_id"
        const val EXTRA_LISTING_TITLE = "extra_listing_title"
    }

    private lateinit var binding: ActivityReviewBinding
    private var orderId: Long = 0L
    private var listingId: Long = 0L
    private var listingTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getLongExtra(EXTRA_ORDER_ID, 0L)
        listingId = intent.getLongExtra(EXTRA_LISTING_ID, 0L)
        listingTitle = intent.getStringExtra(EXTRA_LISTING_TITLE).orEmpty()

        if (orderId <= 0L || listingId <= 0L) {
            Toast.makeText(this, getString(R.string.review_missing_context), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupContent()
        setupActions()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupContent() {
        val displayTitle = if (listingTitle.isBlank()) {
            getString(R.string.review_listing_fallback)
        } else {
            listingTitle
        }
        binding.tvListingName.text = displayTitle
    }

    private fun setupActions() {
        binding.btnSubmitReview.setOnClickListener { submitReview() }
    }

    private fun submitReview() {
        val storeRating = binding.ratingBar.rating.toInt()
        val listingAccuracy = binding.ratingBarAccuracy.rating.toInt()
        val onTimePickup = binding.ratingBarOnTime.rating.toInt()
        val comment = binding.etComment.text?.toString()?.trim().orEmpty()

        if (storeRating < 1) {
            Toast.makeText(this, "Please provide a store rating", Toast.LENGTH_SHORT).show()
            return
        }

        if (listingAccuracy < 1) {
            Toast.makeText(this, "Please rate listing accuracy", Toast.LENGTH_SHORT).show()
            return
        }

        if (onTimePickup < 1) {
            Toast.makeText(this, "Please rate on-time pickup", Toast.LENGTH_SHORT).show()
            return
        }

        if (comment.isBlank()) {
            binding.tilComment.error = getString(R.string.review_comment_required)
            return
        }

        binding.tilComment.error = null
        setSubmitting(true)

        val request = ReviewRequest(
            orderId = orderId,
            listingId = listingId,
            storeRating = storeRating,
            listingAccuracy = listingAccuracy,
            onTimePickup = onTimePickup,
            comment = comment
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.submitReview(request)
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ReviewActivity,
                        getString(R.string.review_submit_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Please login as a consumer to submit reviews"
                        403 -> "You don't have permission to review this item"
                        409 -> "You have already reviewed this item"
                        else -> getString(R.string.review_submit_failed_with_code, response.code())
                    }
                    Toast.makeText(
                        this@ReviewActivity,
                        errorMsg,
                        Toast.LENGTH_LONG
                    ).show()
                    setSubmitting(false)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ReviewActivity,
                    getString(R.string.review_network_error, e.message ?: "unknown"),
                    Toast.LENGTH_SHORT
                ).show()
                setSubmitting(false)
            }
        }
    }

    private fun setSubmitting(submitting: Boolean) {
        binding.btnSubmitReview.isEnabled = !submitting
        binding.ratingBar.isEnabled = !submitting
        binding.ratingBarAccuracy.isEnabled = !submitting
        binding.ratingBarOnTime.isEnabled = !submitting
        binding.etComment.isEnabled = !submitting
        binding.btnSubmitReview.text = if (submitting) {
            getString(R.string.review_submitting)
        } else {
            getString(R.string.review_submit_button)
        }
    }
}
