package com.frh.backend.controller;

import com.frh.backend.Model.Listing;
import com.frh.backend.Model.Store;
import com.frh.backend.Model.ListingPhoto;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
// changed to this route to avoid conflict with ConsumerListingController 
@RequestMapping("/api/supplier/listings")
public class ListingController {

    private static final Path LISTING_UPLOAD_DIR = Paths.get("uploads", "listings").toAbsolutePath();

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private StoreRepository storeRepository;

    // ==========================================
    // CREATE
    // expects: POST /api/supplier/listings?storeId=1
    // body: Listing JSON (title, prices, pickupStart, pickupEnd, expiryAt, ...)
    // ==========================================
@PostMapping
public ResponseEntity<?> createListing(
        @RequestParam(name = "storeId", required = false) Long storeId,
        @RequestBody Listing listing) {

    List<String> errors = new ArrayList<>();

    // 1) Validate / resolve store
    if (storeId == null) {
        errors.add("Store is required");
    } else {
        Optional<Store> storeOpt = storeRepository.findById(storeId);
        if (storeOpt.isEmpty()) {
            errors.add("Store not found with id: " + storeId);
        } else {
            // attach store to listing so it can be persisted
            listing.setStore(storeOpt.get());
        }
    }

    // 2) Validate listing fields (title, prices, pickup window, expiry)
    errors.addAll(validateListing(listing));

    if (!errors.isEmpty()) {
        // 400 with validation messages
        return ResponseEntity.badRequest().body(errors);
    }

    // 3) Save – catch DB errors so they become readable 400s instead of generic 500
    try {
        Listing savedListing = listingRepository.save(listing);
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

    /**
     * Upload a photo for an existing listing.
     * Accepts multipart/form-data with field name "file".
     * Saves the file under /uploads/listings and stores the relative URL in listing_photos.
     */
    @PostMapping(value = "/{id}/photos", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadListingPhoto(@PathVariable Long id,
                                                @RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Photo file is required");
        }

        Optional<Listing> listingOpt = listingRepository.findById(id);
        if (listingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Listing not found with id: " + id);
        }

        try {
            Files.createDirectories(LISTING_UPLOAD_DIR);

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf('.'));
            }

            String filename = "listing_" + id + "_" + System.currentTimeMillis() + ext;
            Path target = LISTING_UPLOAD_DIR.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String relativeUrl = "/uploads/listings/" + filename;

            Listing listing = listingOpt.get();

            // shift existing photos down so the newly uploaded one becomes primary (sortOrder = 1)
            listing.getPhotos().forEach(p -> p.setSortOrder(p.getSortOrder() + 1));

            ListingPhoto photo = new ListingPhoto();
            photo.setListing(listing);
            photo.setPhotoUrl(relativeUrl);
            photo.setSortOrder(1);

            listing.getPhotos().add(photo);
            listingRepository.save(listing);

            return ResponseEntity.ok(relativeUrl);
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Failed to store photo");
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
    public ResponseEntity<List<Listing>> getListingsBySupplier(@PathVariable Long supplierId) {
        List<Listing> listings = listingRepository.findByStore_SupplierProfile_SupplierId(supplierId);
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
}
