package com.frh.backend.controller;


import com.frh.backend.Model.User;
import com.frh.backend.Model.Listing;
import com.frh.backend.Model.Review;
import com.frh.backend.repository.ReviewRepository;
import com.frh.backend.repository.UserRepository;
import com.frh.backend.repository.ListingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository; // 现在这个应该不红了

    @Autowired
    private ListingRepository listingRepository;

    // accept json data from app
    public static class ReviewRequest {
        public Long userId;
        public Long listingId;
        public int rating;
        public String comment;
    }

    // all users can get comment
    @GetMapping("/list/{listingId}")
    public ResponseEntity<?> getReviewsByListing(@PathVariable Long listingId) {

        List<Review> reviews = reviewRepository.findByListing_ListingId(listingId);
        return ResponseEntity.ok(reviews);
    }

    // current user can add comment
    @PostMapping("/add")
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request) {

        User user = userRepository.findById(request.userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = listingRepository.findById(request.listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        Review review = new Review();
        review.setUser(user); // 这里现在应该正常了，因为 User 类型对上了
        review.setListing(listing);
        review.setRating(request.rating);
        review.setComment(request.comment);

        reviewRepository.save(review);

        return ResponseEntity.ok("Review added successfully");
    }

    // current user can delete comment
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, @RequestParam Long currentUserId) {

        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);

        if (reviewOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Review not found");
        }

        Review review = reviewOpt.get();

        // 权限检查
        if (!review.getUser().getUserId().equals(currentUserId)) {
            return ResponseEntity.status(403).body("Permission denied: You can only delete your own review.");
        }

        reviewRepository.delete(review);
        return ResponseEntity.ok("Review deleted successfully");
    }
}