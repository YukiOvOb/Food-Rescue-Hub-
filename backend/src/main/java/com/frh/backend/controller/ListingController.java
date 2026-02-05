package com.frh.backend.controller;

import com.frh.backend.Model.Listing;
import com.frh.backend.Model.ListingPhoto;
import com.frh.backend.Model.Store;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.OrderItemRepository;
import com.frh.backend.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
// changed to this route to avoid conflict with ConsumerListingController 
@RequestMapping("/api/supplier/listings")
public class ListingController {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Path UPLOAD_ROOT = Paths.get("uploads").resolve("listings").toAbsolutePath().normalize();

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
    // ==========================================
    // READ ALL
    // ==========================================
    @GetMapping
    public ResponseEntity<List<Listing>> getAllListings() {
        List<Listing> listings = listingRepository.findAll();
        listings.forEach(this::sortPhotos);
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
            sortPhotos(listingOptional.get());
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

        // Block deletion when the listing has existing order items to prevent FK violations
        if (orderItemRepository.existsByListing_ListingId(id)) {
            return ResponseEntity.status(409)
                    .body("Cannot delete listing because orders already reference it.");
        }

        listingRepository.deleteById(id);
        return ResponseEntity.ok("Listing deleted successfully");
    }

    // ==========================================
    // PHOTO UPLOAD
    // ==========================================
    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadListingPhoto(@PathVariable Long id,
                                                @RequestPart("file") MultipartFile file,
                                                @RequestParam(name = "sortOrder", required = false) Integer sortOrder) {
        Optional<Listing> listingOpt = listingRepository.findById(id);
        if (listingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Listing not found with id: " + id);
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }
        Listing listing = listingOpt.get();
        if (listing.getPhotos() != null && !listing.getPhotos().isEmpty()) {
            return ResponseEntity.badRequest().body("Only one photo is allowed per listing for now.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return ResponseEntity.badRequest().body("File too large. Maximum size is 5MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            return ResponseEntity.badRequest().body("Unsupported file type. Allowed: JPG, PNG, WEBP");
        }

        try {
            Files.createDirectories(UPLOAD_ROOT);
            String extension = getExtension(file.getOriginalFilename(), file.getContentType());
            String filename = UUID.randomUUID() + extension;
            Path destination = UPLOAD_ROOT.resolve(filename);
            Files.copy(file.getInputStream(), destination);

            int nextOrder = (sortOrder != null) ? sortOrder : listing.getPhotos().size() + 1;

            ListingPhoto photo = new ListingPhoto();
            photo.setListing(listing);
            photo.setPhotoUrl("/uploads/listings/" + filename);
            photo.setSortOrder(nextOrder);

            listing.getPhotos().add(photo); // cascade saves the photo
            listingRepository.save(listing);

            return ResponseEntity.ok(photo);
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Failed to store file: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Unexpected error: " + ex.getMessage());
        }
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

    private void sortPhotos(Listing listing) {
        if (listing.getPhotos() != null) {
            listing.getPhotos().sort(Comparator.comparing(ListingPhoto::getSortOrder));
        }
    }

    private String getExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // fallback by mime type
        if ("image/png".equals(contentType)) return ".png";
        if ("image/webp".equals(contentType)) return ".webp";
        return ".jpg";
    }
}
