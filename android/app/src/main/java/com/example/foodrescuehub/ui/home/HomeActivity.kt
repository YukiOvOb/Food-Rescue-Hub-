package com.example.foodrescuehub.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.BannerItem
import com.example.foodrescuehub.data.model.Listing
import com.example.foodrescuehub.data.model.StoreRecommendation
import com.example.foodrescuehub.data.repository.AuthManager
import com.example.foodrescuehub.data.repository.CartManager
import com.example.foodrescuehub.databinding.ActivityHomeBinding
import com.example.foodrescuehub.ui.cart.CartActivity
import com.example.foodrescuehub.ui.orders.OrdersActivity
import com.example.foodrescuehub.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.util.Calendar
import android.graphics.Color

/**
 * Home Activity - Main screen for consumer homepage
 * Displays nearby listings with filtering and search capabilities
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var listingAdapter: ListingAdapter
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var recommendationAdapter: RecommendationAdapter

    // UI components
    private lateinit var tvGreeting: TextView
    private lateinit var searchView: SearchView
    private lateinit var bannerViewPager: ViewPager2
    private lateinit var recommendationsSection: LinearLayout
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var recommendationsProgressBar: android.widget.ProgressBar
    // private lateinit var tabLayout: TabLayout  // Removed
    private lateinit var chipGroupCategories: LinearLayout
    private lateinit var rvListings: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var btnSortFilter: ImageButton
    private lateinit var btnCart: TextView
    private lateinit var tvCartBadge: TextView

    // User location from database (will be fetched from backend)
    private var userLat: Double? = null
    private var userLng: Double? = null

    // User ID (will be fetched from backend based on email)
    private var currentUserId: Long? = null
    private var currentUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AuthManager
        AuthManager.initialize(applicationContext)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Initialize UI components
        initViews()
        setupBanner()
        setupRecommendations()
        setupRecyclerView()
        setupGreeting()
        setupSearchView()
        // setupTabLayout()  // Removed
        setupCategoryChips()
        setupSortFilterButton()
        setupBottomNavigation()
        setupCartButton()

        // Observe ViewModel LiveData
        observeViewModel()
        observeCart()

        // Get current user email from AuthManager
        val currentUser = AuthManager.getCurrentUser()
        currentUserEmail = currentUser?.email

        if (currentUserEmail != null) {
            // Fetch user profile and load recommendations
            fetchUserProfileAndLoadRecommendations()
        } else {
            android.util.Log.e("HomeActivity", "❌ No user logged in")
            // Redirect to login or show error
        }
    }

    override fun onResume() {
        super.onResume()

        // Check if user has changed
        val currentUser = AuthManager.getCurrentUser()
        val newEmail = currentUser?.email

        android.util.Log.d("HomeActivity", "onResume - current email: $currentUserEmail, new email: $newEmail")

        if (newEmail != null && newEmail != currentUserEmail) {
            // User has changed, reload everything
            android.util.Log.d("HomeActivity", "🔄 User changed! Reloading recommendations...")
            currentUserEmail = newEmail
            currentUserId = null  // Reset user ID
            fetchUserProfileAndLoadRecommendations()
        }
    }

    /**
     * Initialize all view references
     */
    private fun initViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        searchView = findViewById(R.id.searchView)
        bannerViewPager = findViewById(R.id.bannerViewPager)
        recommendationsSection = findViewById(R.id.recommendationsSection)
        rvRecommendations = findViewById(R.id.rvRecommendations)
        recommendationsProgressBar = findViewById(R.id.recommendationsProgressBar)
        // tabLayout = findViewById(R.id.tabLayout)  // Removed
        chipGroupCategories = findViewById(R.id.chipGroupCategories)
        rvListings = findViewById(R.id.rvListings)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        btnSortFilter = findViewById(R.id.btnSortFilter)
        btnCart = findViewById(R.id.btnCart)
        tvCartBadge = findViewById(R.id.tvCartBadge)
    }

    /**
     * Setup banner carousel slide - horizontal
     */
    private fun setupBanner() {
        val bannerItems = listOf(
            BannerItem(
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800",
                "Save Food, Save Planet"
            ),
            BannerItem(
                "https://images.unsplash.com/photo-1504754524776-8f4f37790ca0?w=800",
                "Rescue Delicious Food"
            ),
            BannerItem(
                "https://images.unsplash.com/photo-1606787366850-de6330128bfc?w=800",
                "Reduce Food Waste Together"
            )
        )

        bannerAdapter = BannerAdapter(bannerItems)
        bannerViewPager.adapter = bannerAdapter
    }

    /**
     * Setup recommendations RecyclerView
     */
    private fun setupRecommendations() {
        recommendationAdapter = RecommendationAdapter { recommendation ->
            onRecommendationClicked(recommendation)
        }

        rvRecommendations.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendationAdapter
            setHasFixedSize(true)
            // Disable nested scrolling to allow parent scroll
            isNestedScrollingEnabled = false
        }
    }

    /**
     * Fetch user profile from backend and load recommendations with user location
     */
    private fun fetchUserProfileAndLoadRecommendations() {
        recommendationsSection.visibility = View.VISIBLE
        recommendationsProgressBar.visibility = View.VISIBLE
        rvRecommendations.visibility = View.GONE

        if (currentUserEmail == null) {
            android.util.Log.e("HomeActivity", "❌ User email is null")
            return
        }

        // Fetch user profile in background
        lifecycleScope.launch {
            try {
                android.util.Log.d("HomeActivity", "🔍 Fetching user profile for email: $currentUserEmail")

                // Get consumer profile by email
                val response = com.example.foodrescuehub.data.api.RetrofitClient.apiService
                    .getConsumerProfileByEmail(currentUserEmail!!)

                android.util.Log.d("HomeActivity", "Profile response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!

                    // Save user ID and location
                    currentUserId = profile.consumerId
                    userLat = profile.defaultLat
                    userLng = profile.defaultLng

                    android.util.Log.d("HomeActivity", "✅ User profile loaded:")
                    android.util.Log.d("HomeActivity", "   consumerId: $currentUserId")
                    android.util.Log.d("HomeActivity", "   email: ${profile.email}")
                    android.util.Log.d("HomeActivity", "   location: lat=$userLat, lng=$userLng")
                    android.util.Log.d("HomeActivity", "📍 Loading recommendations...")

                    // Load recommendations with user location
                    viewModel.loadRecommendations(currentUserId!!, 5, userLat, userLng)
                } else {
                    android.util.Log.e("HomeActivity", "❌ Failed to fetch user profile: ${response.code()}, body=${response.errorBody()?.string()}")
                    // Load recommendations without location (fallback)
                    if (currentUserId != null) {
                        viewModel.loadRecommendations(currentUserId!!, 5, null, null)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeActivity", "❌ Error fetching user profile: ${e.message}", e)
                // Load recommendations without location (fallback)
                if (currentUserId != null) {
                    viewModel.loadRecommendations(currentUserId!!, 5, null, null)
                }
            }
        }
    }

    /**
     * Handle recommendation item click
     */
    private fun onRecommendationClicked(recommendation: StoreRecommendation) {
        val intent = Intent(this, com.example.foodrescuehub.ui.detail.ProductDetailActivity::class.java).apply {
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_ID, recommendation.listingId)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_TITLE, recommendation.title)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_STORE_NAME, "🏪 ${recommendation.storeName}")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_CATEGORY, recommendation.category)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_DISTANCE, "📍 ${recommendation.getDistanceText()}")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PRICE, recommendation.rescuePrice)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_ORIGINAL_PRICE, recommendation.originalPrice)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_SAVINGS_LABEL, "${recommendation.savingsPercentage}% OFF")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PICKUP_START, recommendation.pickupStart)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PICKUP_END, recommendation.pickupEnd)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_DESCRIPTION, "")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_QTY_AVAILABLE, recommendation.qtyAvailable)

            // Pass photo URL if available
            if (recommendation.photoUrl != null) {
                putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PHOTO_URL, recommendation.photoUrl)
            }
        }
        startActivity(intent)
    }

    /**
     * Setup RecyclerView with adapter
     */
    private fun setupRecyclerView() {
        listingAdapter = ListingAdapter { listing ->
            onBuyClicked(listing)
        }

        binding.rvListings.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = listingAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * Setup greeting based on time of day
     */
    private fun setupGreeting() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }

        binding.tvGreeting.text = greeting
    }

    /**
     * Setup search functionality
     */
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    // Navigate to SearchResultsActivity
                    val intent = Intent(this@HomeActivity, com.example.foodrescuehub.ui.search.SearchResultsActivity::class.java)
                    intent.putExtra("query", it)
                    startActivity(intent)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: implement real-time search suggestion
                return true
            }
        })
    }

    /**
     * Setup tab layout for Mystery Boxes / Regular Items
     */
    /*
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.selectTab(it.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    */

    /**
     * Setup category filter chips
     */
    private fun setupCategoryChips() {
        var selectedChip: LinearLayout? = binding.chipAll // Default selection

        // Set initial selection
        selectedChip?.setBackgroundResource(R.drawable.category_chip_selected)
        updateChipTextColor(selectedChip, true)

        val chips = mapOf(
            binding.chipBakery to "Bakery",
            binding.chipDessert to "Coffee Shop",
            binding.chipCafe to "Cafe",
            binding.chipLightMeal to "Restaurant",
            binding.chipAll to "All"
        )

        chips.forEach { (chip, category) ->
            chip.setOnClickListener {
                // Deselect previous chip
                selectedChip?.let {
                    it.setBackgroundResource(R.drawable.category_chip_background)
                    updateChipTextColor(it, false)
                }

                // Select new chip
                chip.setBackgroundResource(R.drawable.category_chip_selected)
                updateChipTextColor(chip as LinearLayout, true)
                selectedChip = chip

                // Apply filter
                viewModel.filterByCategory(category)
            }
        }
    }

    /**
     * Update chip text color based on selection state
     */
    private fun updateChipTextColor(chip: LinearLayout?, isSelected: Boolean) {
        chip?.let {
            // The second child (index 1) is the TextView containing the category name
            if (it.childCount > 1) {
                val textView = it.getChildAt(1) as? TextView
                textView?.setTextColor(
                    if (isSelected) {
                        Color.WHITE
                    } else {
                        Color.parseColor("#424242")
                    }
                )
            }
        }
    }
    /**
     * Setup sort and filter button
     */
    private fun setupSortFilterButton() {
        binding.btnSortFilter.setOnClickListener {
            val currentSort = viewModel.sortOption.value ?: SortOption.NAME_ASC
            val bottomSheet = SortFilterBottomSheet(currentSort) { sortOption, minPrice, maxPrice ->
                // Apply sort and filter
                viewModel.sortBy(sortOption)
                viewModel.filterByPriceRange(minPrice.toDouble(), maxPrice.toDouble())

                Toast.makeText(
                    this,
                    "Applied: ${getSortLabel(sortOption)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            bottomSheet.show(supportFragmentManager, SortFilterBottomSheet.TAG)
        }
    }

    /**
     * Get display label for sort option
     */
    private fun getSortLabel(option: SortOption): String {
        return when (option) {
            SortOption.NAME_ASC -> "Name (A-Z)"
            SortOption.NAME_DESC -> "Name (Z-A)"
            SortOption.PRICE_LOW_HIGH -> "Price (Low to High)"
            SortOption.PRICE_HIGH_LOW -> "Price (High to Low)"
            SortOption.POPULARITY -> "Most Popular"
            SortOption.DISTANCE -> "Nearest First"
        }
    }

    /**
     * Setup bottom navigation
     */
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_orders -> {
                    startActivity(Intent(this, OrdersActivity::class.java))
                    false
                }

                R.id.nav_location -> {
                    // Navigate to LocationActivity
                    val intent = Intent(this, com.example.foodrescuehub.ui.location.LocationActivity::class.java)
                    startActivity(intent)
                    false
                }
                R.id.nav_profile -> {
                    // Navigate to ProfileActivity
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    false
                }
                else -> false
            }
        }
    }

    /**
     * Observe ViewModel LiveData
     */
    private fun observeViewModel() {
        android.util.Log.d("HomeActivity", "Setting up observers")

        // Observe filtered listings
        viewModel.filteredListings.observe(this) { listings ->
            android.util.Log.d("HomeActivity", "Received ${listings?.size ?: 0} listings")
            try {
                listingAdapter.submitList(listings)
                android.util.Log.d("HomeActivity", "Successfully submitted list to adapter")

                // Show/hide empty state
                if (listings.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvListings.visibility = View.GONE
                    android.util.Log.d("HomeActivity", "Showing empty state")
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvListings.visibility = View.VISIBLE
                    // Force RecyclerView to request layout
                    binding.rvListings.requestLayout()
                    android.util.Log.d("HomeActivity", "Showing listings - RecyclerView visibility: ${binding.rvListings.visibility}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeActivity", "Error displaying listings", e)
                Toast.makeText(this, "Error displaying listings: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Observe recommendations
        viewModel.recommendations.observe(this) { recommendations ->
            android.util.Log.d("HomeActivity", "Received ${recommendations?.size ?: 0} recommendations")

            // Hide progress bar
            recommendationsProgressBar.visibility = View.GONE

            if (recommendations != null && recommendations.isNotEmpty()) {
                recommendationAdapter.submitList(recommendations)
                recommendationsSection.visibility = View.VISIBLE
                rvRecommendations.visibility = View.VISIBLE
                android.util.Log.d("HomeActivity", "Showing recommendations")
            } else {
                recommendationsSection.visibility = View.GONE
                android.util.Log.d("HomeActivity", "No recommendations, hiding section")
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            android.util.Log.d("HomeActivity", "Loading state: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        viewModel.error.observe(this) { error ->
            error?.let {
                android.util.Log.e("HomeActivity", "Error from ViewModel: $it")
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Handle buy button click - Navigate to product detail page
     */
    private fun onBuyClicked(listing: Listing) {
        val intent = Intent(this, com.example.foodrescuehub.ui.detail.ProductDetailActivity::class.java).apply {
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_ID, listing.listingId)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_TITLE, listing.title)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_STORE_NAME, "🏪 ${listing.storeName}")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_CATEGORY, listing.category)

            // Calculate distance (placeholder - should use actual user location)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_DISTANCE, "📍 2.1 km")

            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PRICE, listing.rescuePrice)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_ORIGINAL_PRICE, listing.originalPrice)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_SAVINGS_LABEL, listing.savingsLabel)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PICKUP_START, listing.pickupStart)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PICKUP_END, listing.pickupEnd)
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_DESCRIPTION, listing.description ?: "")
            putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_QTY_AVAILABLE, listing.qtyAvailable)

            // Pass first photo URL if available
            if (!listing.photoUrls.isNullOrEmpty()) {
                putExtra(com.example.foodrescuehub.ui.detail.ProductDetailActivity.EXTRA_LISTING_PHOTO_URL, listing.photoUrls[0])
            }
        }
        startActivity(intent)
    }

    /**
     * Setup cart button
     */
    private fun setupCartButton() {
        binding.btnCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Observe cart changes
     */
    private fun observeCart() {
        CartManager.itemCount.observe(this) { count ->
            if (count > 0) {
                binding.tvCartBadge.visibility = View.VISIBLE
                binding.tvCartBadge.text = count.toString()
            } else {
                binding.tvCartBadge.visibility = View.GONE
            }
        }
    }
}
