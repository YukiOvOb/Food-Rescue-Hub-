package com.frh.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "listing_reviews",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_listing_review_order_listing_consumer",
          columnNames = {"order_id", "listing_id", "consumer_id"})
    },
    indexes = {
      @Index(name = "idx_listing_review_listing_id", columnList = "listing_id"),
      @Index(name = "idx_listing_review_consumer_id", columnList = "consumer_id"),
      @Index(name = "idx_listing_review_created_at", columnList = "created_at")
    })
@Data
public class ListingReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "review_id", nullable = false)
  private Long reviewId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id", nullable = false)
  private Listing listing;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "consumer_id", nullable = false)
  private ConsumerProfile consumer;

  @Column(name = "rating", nullable = false)
  private Integer rating;

  @Column(name = "listing_accuracy", nullable = false)
  private Integer listingAccuracy;

  @Column(name = "on_time_pickup", nullable = false)
  private Integer onTimePickup;

  @Column(name = "comment", nullable = false, length = 300)
  private String comment;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
