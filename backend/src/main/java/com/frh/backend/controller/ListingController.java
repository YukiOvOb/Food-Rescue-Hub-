package com.frh.backend.controller;

import com.frh.backend.Model.Listing;
import com.frh.backend.repository.ListingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingRepository listingRepository;

    // create
    @PostMapping
    public ResponseEntity<?> createListing(@RequestBody Listing listing) {
        // use validation
        List<String> errors = validateListing(listing);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }


        // delete setCreatedAt  setStatus
        //  in Entity  @CreationTimestamp and default value can solve them

        // save
        Listing savedListing = listingRepository.save(listing);
        return ResponseEntity.ok(savedListing);
    }

    //search all
    @GetMapping
    public ResponseEntity<List<Listing>> getAllListings() {
        List<Listing> listings = listingRepository.findAll();
        return ResponseEntity.ok(listings);
    }

    // search 1
    @GetMapping("/{id}")
    public ResponseEntity<?> getListingById(@PathVariable Long id) {
        Optional<Listing> listingOptional = listingRepository.findById(id);

        if (listingOptional.isPresent()) {
            return ResponseEntity.ok(listingOptional.get());
        } else {
            return ResponseEntity.status(404).body("Listing not found with id: " + id);
        }
    }

    // update
    @PutMapping("/{id}")
    public ResponseEntity<?> updateListing(@PathVariable Long id, @RequestBody Listing listingDetails) {
        Optional<Listing> existingListingOpt = listingRepository.findById(id);
        if (existingListingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Listing not found with id: " + id);
        }

        Listing existingListing = existingListingOpt.get();

        // validate data
        List<String> errors = validateListing(listingDetails);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // update field
        existingListing.setTitle(listingDetails.getTitle());
        existingListing.setDescription(listingDetails.getDescription());
        existingListing.setOriginalPrice(listingDetails.getOriginalPrice());
        existingListing.setRescuePrice(listingDetails.getRescuePrice());
        existingListing.setPickupStart(listingDetails.getPickupStart());
        existingListing.setPickupEnd(listingDetails.getPickupEnd());



        Listing updatedListing = listingRepository.save(existingListing);
        return ResponseEntity.ok(updatedListing);
    }

    // delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteListing(@PathVariable Long id) {
        if (!listingRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Listing not found with id: " + id);
        }

        listingRepository.deleteById(id);
        return ResponseEntity.ok("Listing deleted successfully");
    }

    // validation
    private List<String> validateListing(Listing listing) {
        List<String> errors = new ArrayList<>();

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

        if (!errors.isEmpty()) {
            return errors;
        }

        if (listing.getRescuePrice().compareTo(listing.getOriginalPrice()) >= 0) {
            errors.add("Rescue price must be lower than original price");
        }

        if (listing.getPickupStart().isBefore(LocalDateTime.now())) {
            errors.add("Pickup start time must be in the future");
        }

        if (listing.getPickupEnd().isBefore(listing.getPickupStart())) {
            errors.add("Pickup end time cannot be before start time");
        }

        return errors;
    }
}