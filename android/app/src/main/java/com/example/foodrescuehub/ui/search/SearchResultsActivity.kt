package com.example.foodrescuehub.ui.search

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.StoreRecommendation
import com.example.foodrescuehub.data.repository.AuthManager
import com.example.foodrescuehub.ui.home.RecommendationAdapter
import kotlinx.coroutines.launch

/**
 * Search Results Activity
 * Display search results with multiple sorting options
 */
class SearchResultsActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var rvResults: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressLayout: LinearLayout
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvResultsCount: TextView
    private lateinit var spinnerSort: Spinner
    private lateinit var btnBack: ImageButton

    private lateinit var adapter: RecommendationAdapter
    private var currentResults: List<StoreRecommendation> = emptyList()
    private var currentUserId: Long? = null
    private var userLat: Double? = null
    private var userLng: Double? = null
    private var currentQuery: String = ""

    // Sort options
    private enum class SortOption {
        RECOMMENDED,  // ML-powered recommendation sorting
        DISTANCE,     // Sort by distance
        PRICE,        // Sort by price
        RATING        // Sort by rating
    }

    private var currentSortOption = SortOption.RECOMMENDED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        // Initialize views
        searchView = findViewById(R.id.searchView)
        rvResults = findViewById(R.id.rvResults)
        progressBar = findViewById(R.id.progressBar)
        progressLayout = findViewById(R.id.progressLayout)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        tvResultsCount = findViewById(R.id.tvResultsCount)
        spinnerSort = findViewById(R.id.spinnerSort)
        btnBack = findViewById(R.id.btnBack)

        // Setup RecyclerView
        adapter = RecommendationAdapter { recommendation ->
            // Handle item click - navigate to detail page
            Toast.makeText(this, "Clicked: ${recommendation.title}", Toast.LENGTH_SHORT).show()
        }
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = adapter

        // Setup sort spinner
        setupSortSpinner()

        // Get user info
        val currentUser = AuthManager.getCurrentUser()
        val userEmail = currentUser?.email

        if (userEmail != null) {
            fetchUserProfile(userEmail)
        }

        // Get initial search query from intent
        currentQuery = intent.getStringExtra("query") ?: ""
        searchView.setQuery(currentQuery, false)

        // Setup search view listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    currentQuery = it
                    performSearch(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: implement real-time search
                return false
            }
        })

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Perform initial search if query is provided
        if (currentQuery.isNotEmpty() && currentUserId != null) {
            performSearch(currentQuery)
        }
    }

    /**
     * Setup sort spinner
     */
    private fun setupSortSpinner() {
        val sortOptions = arrayOf("Recommended", "Distance", "Price", "Rating")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = spinnerAdapter

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSortOption = when (position) {
                    0 -> SortOption.RECOMMENDED
                    1 -> SortOption.DISTANCE
                    2 -> SortOption.PRICE
                    3 -> SortOption.RATING
                    else -> SortOption.RECOMMENDED
                }
                applySorting()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Fetch user profile by email
     */
    private fun fetchUserProfile(email: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getConsumerProfileByEmail(email)
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    currentUserId = profile.consumerId
                    userLat = profile.defaultLat
                    userLng = profile.defaultLng

                    // Perform search if query is already set
                    if (currentQuery.isNotEmpty()) {
                        performSearch(currentQuery)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchResultsActivity, "Failed to load user profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Perform search based on current sort option
     */
    private fun performSearch(query: String) {
        if (currentUserId == null) {
            Toast.makeText(this, "Please wait, loading user profile...", Toast.LENGTH_SHORT).show()
            return
        }

        when (currentSortOption) {
            SortOption.RECOMMENDED -> searchWithMLRecommendations(query)
            else -> searchBasic(query) // For other sort options, use basic search then apply sorting
        }
    }

    /**
     * Search with ML-powered recommendations
     */
    private fun searchWithMLRecommendations(query: String) {
        progressLayout.visibility = View.VISIBLE
        rvResults.visibility = View.GONE
        layoutEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.searchWithRecommendations(
                    consumerId = currentUserId!!,
                    query = query,
                    topK = 20,
                    latitude = userLat,
                    longitude = userLng
                )

                progressLayout.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val recommendations = response.body()!!.recommendations
                    currentResults = recommendations
                    displayResults(recommendations)
                } else {
                    showEmptyState("No results found for \"$query\"")
                }
            } catch (e: Exception) {
                progressLayout.visibility = View.GONE
                showEmptyState("Error: ${e.message}")
            }
        }
    }

    /**
     * Basic search (fallback for distance/price/rating sorting)
     */
    private fun searchBasic(query: String) {
        // Use ML recommendations as base, then apply client-side sorting
        searchWithMLRecommendations(query)
    }

    /**
     * Apply sorting to current results
     */
    private fun applySorting() {
        if (currentResults.isEmpty()) return

        val sortedResults = when (currentSortOption) {
            SortOption.RECOMMENDED -> currentResults // Already sorted by ML
            SortOption.DISTANCE -> currentResults.sortedBy { it.distance ?: Double.MAX_VALUE }
            SortOption.PRICE -> currentResults.sortedBy { it.rescuePrice }
            SortOption.RATING -> currentResults.sortedByDescending { it.avgRating ?: 0.0 }
        }

        displayResults(sortedResults)
    }

    /**
     * Display search results
     */
    private fun displayResults(results: List<StoreRecommendation>) {
        if (results.isEmpty()) {
            showEmptyState("No results found")
        } else {
            progressLayout.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
            rvResults.visibility = View.VISIBLE
            adapter.submitList(results)
            tvResultsCount.text = "${results.size} results"
        }
    }

    /**
     * Show empty state
     */
    private fun showEmptyState(message: String) {
        progressLayout.visibility = View.GONE
        rvResults.visibility = View.GONE
        layoutEmptyState.visibility = View.VISIBLE
        tvResultsCount.text = "0 results"
        findViewById<TextView>(R.id.tvEmptyMessage).text = message
    }
}
