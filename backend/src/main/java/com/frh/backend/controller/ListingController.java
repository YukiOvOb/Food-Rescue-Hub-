package com.frh.backend.controller;

import com.frh.backend.Model.Listing;
import com.frh.backend.dto.ListingDTO;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
// changed to this route to avoid conflict with ConsumerListingController 
@RequestMapping("/api/supplier/listings")
public class ListingController {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ListingService listingService;

    // ==========================================
    // CREATE
    // expects: POST /api/supplier/listings?storeId=1
    // body: Listing JSON (title, prices, pickupStart, pickupEnd, expiryAt, ...)
    // ==========================================
    @PostMapping
    public ResponseEntity<?> createListing(
            @RequestParam(name = "storeId", required = false) Long storeId,
            @RequestBody ListingDTO listingDto) {

        List<String> errors = new ArrayList<>();

        // 1) Validate store
        if (storeId == null) {
            errors.add("Store is required");
        } else if (!storeRepository.existsById(storeId)) {
            errors.add("Store not found with id: " + storeId);
        }

        // 2) Validate listing fields (title, prices, pickup window, expiry)
        errors.addAll(validateListingDto(listingDto));

    if (!errors.isEmpty()) {
        // 400 with validation messages
        return ResponseEntity.badRequest().body(errors);
    }

    // 3) Save – catch DB errors so they become readable 400s instead of generic 500
    try {
        ListingDTO savedListing = listingService.createListing(listingDto, storeId);
        return ResponseEntity.ok(savedListing);

    } catch (DataIntegrityViolationException ex) {
        ex.printStackTrace(); // will show exact column / constraint in your terminal

        String msg = "Database error: " + ex.getMostSpecificCause().getMessage();
        return ResponseEntity.badRequest().body(msg);

    } catch (Exception ex) {
        ex.printStackTrace();
        String msg = "Unexpected server error: " + ex.getMessage();
        return ResponseEntity.status(500).body(msg);
    }
}
    // ==========================================
    // READ ALL
    // ==========================================
    @GetMapping
    public ResponseEntity<List<Listing>> getAllListings() {
        List<Listing> listings = listingRepository.findAll();
        return ResponseEntity.ok(listings);
    }

    // ==========================================
    // READ ALL BY SUPPLIER
    // ==========================================
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<ListingDTO>> getListingsBySupplier(@PathVariable Long supplierId) {
        List<ListingDTO> listings = listingService.getListingsBySupplier(supplierId);
        return ResponseEntity.ok(listings);
    }

    // ==========================================
    // READ ONE
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getListingById(@PathVariable Long id) {
        Optional<Listing> listingOptional = listingRepository.findById(id);

        if (listingOptional.isPresent()) {
            return ResponseEntity.ok(listingOptional.get());
        } else {
            return ResponseEntity.status(404).body("Listing not found with id: " + id);
        }
    }

    // ==========================================
    // UPDATE
    // ==========================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateListing(@PathVariable Long id,
                                           @RequestBody Listing listingDetails) {

        Optional<Listing> existingListingOpt = listingRepository.findById(id);
        if (existingListingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Listing not found with id: " + id);
        }

        Listing existingListing = existingListingOpt.get();

        List<String> errors = validateListing(listingDetails);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        existingListing.setTitle(listingDetails.getTitle());
        existingListing.setDescription(listingDetails.getDescription());
        existingListing.setOriginalPrice(listingDetails.getOriginalPrice());
        existingListing.setRescuePrice(listingDetails.getRescuePrice());
        existingListing.setPickupStart(listingDetails.getPickupStart());
        existingListing.setPickupEnd(listingDetails.getPickupEnd());
        existingListing.setExpiryAt(listingDetails.getExpiryAt());  // include expiry

        Listing updatedListing = listingRepository.save(existingListing);
        return ResponseEntity.ok(updatedListing);
    }

    // ==========================================
    // DELETE
    // ==========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteListing(@PathVariable Long id) {
        if (!listingRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Listing not found with id: " + id);
        }

        listingRepository.deleteById(id);
        return ResponseEntity.ok("Listing deleted successfully");
    }

    // ==========================================
    // Validation helper
    // ==========================================
    private List<String> validateListing(Listing listing) {
        List<String> errors = new ArrayList<>();

        // basic required fields
        if (listing.getTitle() == null || listing.getTitle().trim().isEmpty()) {
            errors.add("Title is required");
        }
        if (listing.getOriginalPrice() == null) {
            errors.add("Original price is required");
        } else if (listing.getOriginalPrice().doubleValue() <= 0) {
            errors.add("Original price must be greater than 0");
        }
        if (listing.getRescuePrice() == null) {
            errors.add("Rescue price is required");
        } else if (listing.getRescuePrice().doubleValue() < 0) {
            errors.add("Rescue price cannot be negative");
        }
        if (listing.getPickupStart() == null) {
            errors.add("Pickup start time is required");
        }
        if (listing.getPickupEnd() == null) {
            errors.add("Pickup end time is required");
        }
        if (listing.getExpiryAt() == null) {
            errors.add("Expiry time is required");
        }

        // if any required fields missing, stop here to avoid NullPointerException
        if (!errors.isEmpty()) {
            return errors;
        }

        // price relationship
        if (listing.getRescuePrice().compareTo(listing.getOriginalPrice()) >= 0) {
            errors.add("Rescue price must be lower than original price");
        }

        // pickup window: start must be in the future
        if (listing.getPickupStart().isBefore(LocalDateTime.now())) {
            errors.add("Pickup start time must be in the future");
        }

        // pickup end must be after start
        if (listing.getPickupEnd().isBefore(listing.getPickupStart())) {
            errors.add("Pickup end time cannot be before start time");
        }

        // expiry must be after pickup end
        if (listing.getExpiryAt().isBefore(listing.getPickupEnd())) {
            errors.add("Expiry time must be after pickup end time");
        }

        return errors;
    }

    private List<String> validateListingDto(ListingDTO listingDto) {
        List<String> errors = new ArrayList<>();

        if (listingDto.getTitle() == null || listingDto.getTitle().trim().isEmpty()) {
            errors.add("Title is required");
        }
        if (listingDto.getOriginalPrice() == null) {
            errors.add("Original price is required");
        } else if (listingDto.getOriginalPrice().doubleValue() <= 0) {
            errors.add("Original price must be greater than 0");
        }
        if (listingDto.getRescuePrice() == null) {
            errors.add("Rescue price is required");
        } else if (listingDto.getRescuePrice().doubleValue() < 0) {
            errors.add("Rescue price cannot be negative");
        }
        if (listingDto.getPickupStart() == null) {
            errors.add("Pickup start time is required");
        }
        if (listingDto.getPickupEnd() == null) {
            errors.add("Pickup end time is required");
        }
        if (listingDto.getExpiryAt() == null) {
            errors.add("Expiry time is required");
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        if (listingDto.getRescuePrice().compareTo(listingDto.getOriginalPrice()) >= 0) {
            errors.add("Rescue price must be lower than original price");
        }

        if (listingDto.getPickupStart().isBefore(LocalDateTime.now())) {
            errors.add("Pickup start time must be in the future");
        }

        if (listingDto.getPickupEnd().isBefore(listingDto.getPickupStart())) {
            errors.add("Pickup end time cannot be before start time");
        }

        if (listingDto.getExpiryAt().isBefore(listingDto.getPickupEnd())) {
            errors.add("Expiry time must be after pickup end time");
        }

        return errors;
    }
}
