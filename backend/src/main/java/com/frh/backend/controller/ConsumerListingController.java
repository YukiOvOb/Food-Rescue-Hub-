package com.frh.backend.controller;

import com.frh.backend.dto.ListingDTO;
import com.frh.backend.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Consumer Listing operations
 * Provides read-only endpoints for consumer homepage to fetch available listings
 * For supplier CRUD operations, see SupplierListingController
 */
@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "*") // Allow requests from Android app
public class ConsumerListingController {

    @Autowired
    private ListingService listingService;

    /**
     * Get all active listings with available inventory
     * GET /api/listings
     *
     * @return List of all active listings
     */
    @GetMapping
    public ResponseEntity<List<ListingDTO>> getAllListings() {
        List<ListingDTO> listings = listingService.getAllActiveListings();
        return ResponseEntity.ok(listings);
    }

    /**
     * Get nearby listings based on user location
     * GET /api/listings/nearby?lat=1.3521&lng=103.8198&radius=5
     *
     * @param lat User's latitude
     * @param lng User's longitude
     * @param radius Search radius in km (optional, default: 5km)
     * @return List of nearby listings within the specified radius
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<ListingDTO>> getNearbyListings(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false, defaultValue = "5.0") Double radius) {
        List<ListingDTO> listings = listingService.getNearbyListings(lat, lng, radius);
        return ResponseEntity.ok(listings);
    }

    /**
     * Get listings filtered by category
     * GET /api/listings/category/Bakery
     *
     * @param category Store category (e.g., Bakery, Cafe, Restaurant, etc.)
     * @return List of listings matching the category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ListingDTO>> getListingsByCategory(
            @PathVariable String category) {
        List<ListingDTO> listings = listingService.getListingsByCategory(category);
        return ResponseEntity.ok(listings);
    }
}
