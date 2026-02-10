package com.frh.backend.repository;

import com.frh.backend.Model.ListingReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingReviewRepository extends JpaRepository<ListingReview, Long> {

    List<ListingReview> findByListing_ListingIdOrderByCreatedAtDesc(Long listingId);

    boolean existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
        Long orderId,
        Long listingId,
        Long consumerId
    );
}
