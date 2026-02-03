package com.example.foodrescuehub.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.Listing
import com.example.foodrescuehub.data.repository.AuthManager
import com.example.foodrescuehub.data.repository.CartManager
import com.example.foodrescuehub.databinding.ActivityHomeBinding
import com.example.foodrescuehub.ui.cart.CartActivity
import com.example.foodrescuehub.ui.orders.OrdersActivity
import com.example.foodrescuehub.ui.profile.ProfileActivity
import com.google.android.material.tabs.TabLayout
import java.util.Calendar

/**
 * Home Activity - Main screen for consumer homepage
 * Displays nearby listings with filtering and search capabilities
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var listingAdapter: ListingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AuthManager
        AuthManager.initialize(applicationContext)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Setup UI components
        setupRecyclerView()
        setupGreeting()
        setupSearchView()
        setupTabLayout()
        setupCategoryChips()
        setupSortFilterButton()
        setupBottomNavigation()
        setupCartButton()

        // Observe ViewModel LiveData
        observeViewModel()
        observeCart()
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
                query?.let { viewModel.searchListings(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    // Reset to filtered listings when search is cleared
                    viewModel.searchListings("")
                }
                return true
            }
        })
    }

    /**
     * Setup tab layout for Mystery Boxes / Regular Items
     */
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

    /**
     * Setup category filter chips
     */
    private fun setupCategoryChips() {
        val chipMap = mapOf(
            R.id.chipBakery to "Bakery",
            R.id.chipDessert to "Coffee Shop",  // Map Dessert chip to Coffee Shop
            R.id.chipCafe to "Cafe",
            R.id.chipLightMeal to "Restaurant",
            R.id.chipAll to "All"
        )

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChipId = checkedIds[0]
                val category = chipMap[selectedChipId] ?: "All"
                viewModel.filterByCategory(category)
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
