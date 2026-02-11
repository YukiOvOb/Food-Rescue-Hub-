package com.frh.backend.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;


import com.frh.backend.dto.ListingDto;
import com.frh.backend.model.Listing;
import com.frh.backend.model.ListingPhoto;
import com.frh.backend.repository.ListingRepository;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.service.ListingService;
import com.frh.backend.service.PhotoStorageService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@RestController
// changed to this route to avoid conflict with ConsumerListingController
@RequestMapping("/api/supplier/listings")
public class ListingController {

  @Autowired private ListingRepository listingRepository;

  @Autowired private StoreRepository storeRepository;

  @Autowired private ListingService listingService;

  @Autowired private PhotoStorageService photoStorageService;

  // ==========================================
  // CREATE
  // expects: POST /api/supplier/listings?storeId=1
  // body: Listing JSON (title, prices, pickupStart, pickupEnd, expiryAt, ...)
  // ==========================================
  @PostMapping
  public ResponseEntity<?> createListing(
      @RequestParam(name = "storeId", required = false) Long storeId,
      @RequestBody ListingDto listingDto) {

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
      ListingDto savedListing = listingService.createListing(listingDto, storeId);
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
   * Upload a photo for an existing listing. Accepts multipart/form-data with field name "file".
   * Saves the file under /uploads/listings and stores the relative URL in listing_photos.
   */
  @PostMapping(value = "/{id}/photos", consumes = "multipart/form-data")
  public ResponseEntity<?> uploadListingPhoto(
      @PathVariable Long id, @RequestPart("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest().body("Photo file is required");
    }

    Optional<Listing> listingOpt = listingRepository.findById(id);
    if (listingOpt.isEmpty()) {
      return ResponseEntity.status(404).body("Listing not found with id: " + id);
    }

    try {
      String photoUrl = photoStorageService.store(id, file);

      Listing listing = listingOpt.get();

      // shift existing photos down so the newly uploaded one becomes primary (sortOrder = 1)
      listing.getPhotos().forEach(p -> p.setSortOrder(p.getSortOrder() + 1));

      ListingPhoto photo = new ListingPhoto();
      photo.setListing(listing);
      photo.setPhotoUrl(photoUrl);
      photo.setSortOrder(1);

      listing.getPhotos().add(photo);
      listingRepository.save(listing);

      return ResponseEntity.ok(photoUrl);
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
  public ResponseEntity<List<ListingDto>> getListingsBySupplier(@PathVariable Long supplierId) {
    List<ListingDto> listings = listingService.getListingsBySupplier(supplierId);
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
  public ResponseEntity<?> updateListing(
      @PathVariable Long id, @RequestBody ListingDto listingDetails) {

    if (!listingRepository.existsById(id)) {
      return ResponseEntity.status(404).body("Listing not found with id: " + id);
    }

    List<String> errors = validateListingDto(listingDetails);
    if (!errors.isEmpty()) {
      return ResponseEntity.badRequest().body(errors);
    }

    try {
      ListingDto updatedListing = listingService.updateListing(id, listingDetails);
      return ResponseEntity.ok(updatedListing);
    } catch (DataIntegrityViolationException ex) {
      ex.printStackTrace();
      String msg = "Database error: " + ex.getMostSpecificCause().getMessage();
      return ResponseEntity.badRequest().body(msg);
    } catch (Exception ex) {
      ex.printStackTrace();
      String msg = "Unexpected server error: " + ex.getMessage();
      return ResponseEntity.status(500).body(msg);
    }
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

  private List<String> validateListingDto(ListingDto listingDto) {
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
