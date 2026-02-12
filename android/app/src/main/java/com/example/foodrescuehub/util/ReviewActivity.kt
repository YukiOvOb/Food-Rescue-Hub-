package com.example.foodrescuehub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.api.ApiService
import com.example.foodrescuehub.data.model.ReviewRequest
import com.example.foodrescuehub.ui.detail.ProductDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activity for submitting user reviews with star ratings and comments.
 * Navigates back to the Product Detail page upon successful submission.
 */
class ReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // Retrieve Listing ID passed from the previous activity
        val listingId = intent.getLongExtra("EXTRA_LISTING_ID", -1L)

        // Retrieve User ID from shared preferences session
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getLong("KEY_USER_ID", -1L)

        // Validation: Close activity if Listing ID is missing
        if (listingId == -1L) {
            Toast.makeText(this, "Error: Listing info missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Validation: Redirect or alert if user is not logged in
        if (userId == -1L) {
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI components
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitReview)

        // Initialize API service via Retrofit
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val comment = etComment.text.toString().trim()

            // Input validation: Ensure star rating and comment text are provided
            if (rating == 0) {
                Toast.makeText(this, "Please select a rating star", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (comment.isEmpty()) {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // UI feedback: Disable button to prevent duplicate submissions
            btnSubmit.isEnabled = false
            btnSubmit.text = "Submitting..."

            // Construct the review request payload
            val request = ReviewRequest(
                userId = userId,
                listingId = listingId,
                rating = rating,
                comment = comment
            )

            // Execute the network request within a coroutine scope
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = apiService.submitReview(request)
                    if (response.isSuccessful) {
                        Toast.makeText(this@ReviewActivity, "Review Submitted!", Toast.LENGTH_SHORT).show()

                        // IMPORTANT: Redirect to ProductDetailActivity instead of simply finishing
                        val intent = Intent(this@ReviewActivity, ProductDetailActivity::class.java)

                        // Pass the listing ID back to ensure the detail page displays the correct item
                        intent.putExtra(ProductDetailActivity.EXTRA_LISTING_ID, listingId)

                        // Set flags to clear the activity stack and reuse the existing Detail page instance
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                        startActivity(intent)
                        finish() // Close the current review submission page
                    } else {
                        Toast.makeText(this@ReviewActivity, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Submit Review"
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ReviewActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Review"
                }
            }
        }
    }
}