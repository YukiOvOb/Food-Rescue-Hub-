package com.frh.backend.controller;

import com.frh.backend.dto.CreateListingReviewRequest;
import com.frh.backend.dto.ErrorResponse;
import com.frh.backend.dto.ListingReviewResponse;
import com.frh.backend.service.ListingReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ListingReviewController {

    private final ListingReviewService listingReviewService;

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<?> getReviewsByListing(@PathVariable Long listingId) {
        try {
            List<ListingReviewResponse> reviews = listingReviewService.getReviewsByListing(listingId);
            return ResponseEntity.ok(reviews);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error retrieving listing reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve reviews"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createReview(
        @Valid @RequestBody CreateListingReviewRequest request,
        HttpSession session
    ) {
        try {
            Long consumerId = getCurrentConsumerId(session);
            ListingReviewResponse created = listingReviewService.createReview(consumerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error creating listing review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create review"));
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, HttpSession session) {
        try {
            Long consumerId = getCurrentConsumerId(session);
            listingReviewService.deleteReview(reviewId, consumerId);
            return ResponseEntity.ok().body("Review deleted successfully");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error deleting listing review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete review"));
        }
    }

    private Long getCurrentConsumerId(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        String userRole = (String) session.getAttribute("USER_ROLE");
        if (userId == null || !"CONSUMER".equals(userRole)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorised");
        }
        return userId;
    }
}
