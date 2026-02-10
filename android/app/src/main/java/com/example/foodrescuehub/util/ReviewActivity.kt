package com.example.foodrescuehub

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.api.ApiService
import com.example.foodrescuehub.data.model.ReviewRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // getlisting ID (from last page Intent )
        // "EXTRA_LISTING_ID"
        val listingId = intent.getLongExtra("EXTRA_LISTING_ID", -1L)

        // get user ID (from UserSession)

        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getLong("KEY_USER_ID", -1L)


        // security check
        if (listingId == -1L) {
            Toast.makeText(this, "Error: Listing info missing!", Toast.LENGTH_SHORT).show()
            finish() // if no listing id close the page
            return
        }

        if (userId == -1L) {
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show()
            // jump to LoginActivity
            finish()
            return
        }

        //submit review
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitReview)

        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val comment = etComment.text.toString().trim()


            if (rating == 0) {
                Toast.makeText(this, "Please select a rating star", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (comment.isEmpty()) {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable the button to prevent duplicate submissions.
            btnSubmit.isEnabled = false
            btnSubmit.text = "Submitting..."

            // data
            val request = ReviewRequest(
                userId = userId,       // User ID
                listingId = listingId, //  Listing ID
                rating = rating,
                comment = comment
            )

            // launch request
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = apiService.submitReview(request)
                    if (response.isSuccessful) {
                        Toast.makeText(this@ReviewActivity, "Review Submitted!", Toast.LENGTH_SHORT).show()
                        finish() // succeed return last page
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