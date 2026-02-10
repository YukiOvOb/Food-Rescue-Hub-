package com.frh.backend.repository;

import com.frh.backend.Model.Review; // 🔥 确保这里引入的是正确的 Review
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByListing_ListingId(Long listingId);
}