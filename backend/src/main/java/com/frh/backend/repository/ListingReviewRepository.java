package com.frh.backend.repository;

import com.frh.backend.model.ListingReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingReviewRepository extends JpaRepository<ListingReview, Long> {

  List<ListingReview> findByListing_ListingIdOrderByCreatedAtDesc(Long listingId);

  List<ListingReview> findByConsumer_ConsumerIdOrderByCreatedAtDesc(Long consumerId);

  List<ListingReview> findByConsumer_ConsumerIdAndOrder_OrderIdOrderByCreatedAtDesc(
      Long consumerId, Long orderId);

  boolean existsByOrder_OrderIdAndListing_ListingIdAndConsumer_ConsumerId(
      Long orderId, Long listingId, Long consumerId);
}
