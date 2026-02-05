package com.frh.backend.service;

import com.frh.backend.Model.Listing;
import com.frh.backend.Model.ListingPhoto;
import com.frh.backend.dto.ListingDTO;
import com.frh.backend.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    /**
     * Get all active listings with available inventory
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getAllActiveListings() {
        List<Listing> listings = listingRepository.findAllActiveListingsWithDetails();
        return listings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get nearby listings based on user location
     * @param lat User's latitude
     * @param lng User's longitude
     * @param radius Search radius in kilometers (default: 5km)
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getNearbyListings(Double lat, Double lng, Double radius) {
        if (radius == null || radius <= 0) {
            radius = 5.0; // Default 5km radius
        }
        List<Listing> listings = listingRepository.findNearbyListings(lat, lng, radius);
        return listings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filter listings by category
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getListingsByCategory(String category) {
        List<Listing> allListings = listingRepository.findAllActiveListingsWithDetails();
        return allListings.stream()
                .filter(listing -> {
                    String storeCategory = getStoreCategory(listing);
                    return category.equalsIgnoreCase("All") ||
                           category.equalsIgnoreCase(storeCategory);
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Listing entity to ListingDTO
     */
    private ListingDTO convertToDTO(Listing listing) {
        ListingDTO dto = new ListingDTO();

        // Basic listing info
        dto.setListingId(listing.getListingId());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setOriginalPrice(listing.getOriginalPrice());
        dto.setRescuePrice(listing.getRescuePrice());
        dto.setPickupStart(listing.getPickupStart());
        dto.setPickupEnd(listing.getPickupEnd());
        dto.setExpiryAt(listing.getExpiryAt());
        dto.setStatus(listing.getStatus());

        // Store info
        if (listing.getStore() != null) {
            dto.setStoreId(listing.getStore().getStoreId());
            dto.setStoreName(listing.getStore().getStoreName());
            dto.setStoreDescription(listing.getStore().getDescription());
            dto.setAddressLine(listing.getStore().getAddressLine());
            dto.setPostalCode(listing.getStore().getPostalCode());
            dto.setLat(listing.getStore().getLat());
            dto.setLng(listing.getStore().getLng());
            dto.setPickupInstructions(listing.getStore().getPickupInstructions());
            dto.setOpeningHours(listing.getStore().getOpeningHours());

            // Category from StoreType
            dto.setCategory(getStoreCategory(listing));
        }

        // Inventory
        if (listing.getInventory() != null) {
            dto.setQtyAvailable(listing.getInventory().getQtyAvailable());
            dto.setQtyReserved(listing.getInventory().getQtyReserved());
        }

        // Photos
        if (listing.getPhotos() != null && !listing.getPhotos().isEmpty()) {
            List<String> photoUrls = listing.getPhotos().stream()
                    .sorted((p1, p2) -> Integer.compare(p1.getSortOrder(), p2.getSortOrder()))
                    .map(ListingPhoto::getPhotoUrl)
                    .collect(Collectors.toList());
            dto.setPhotoUrls(photoUrls);
        }

        // Calculated fields
        dto.setTimeRemaining(calculateTimeRemaining(listing.getPickupEnd()));
        dto.setSavingsAmount(listing.getOriginalPrice().subtract(listing.getRescuePrice()));
        dto.setSavingsLabel("Worth $" + listing.getOriginalPrice().intValue() + "+");

        return dto;
    }

    /**
     * Get store category from listing
     */
    private String getStoreCategory(Listing listing) {
        if (listing.getStore() != null &&
            listing.getStore().getSupplierProfile() != null &&
            listing.getStore().getSupplierProfile().getStoreType() != null) {
            return listing.getStore().getSupplierProfile().getStoreType().getTypeName();
        }
        return "Unknown";
    }

    /**
     * Calculate time remaining until pickup end
     */
    private String calculateTimeRemaining(LocalDateTime pickupEnd) {
        if (pickupEnd == null) {
            return "N/A";
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(pickupEnd)) {
            return "Expired";
        }

        Duration duration = Duration.between(now, pickupEnd);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m left";
        } else {
            return minutes + "m left";
        }
    }
}
