package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "store_ratings",
    indexes = {
      @Index(name = "idx_store_rating_store_id", columnList = "store_id"),
      @Index(name = "idx_store_rating_consumer_id", columnList = "consumer_id"),
      @Index(name = "idx_store_rating_rating", columnList = "rating"),
      @Index(name = "idx_store_rating_created_at", columnList = "created_at")
    })
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StoreRating {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "rating_id", nullable = false)
  private Long ratingId;

  // --- Relationships ---

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  @JsonIgnoreProperties({"listings", "orders", "supplierProfile", "ratings"})
  private Store store;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "consumer_id", nullable = false)
  @JsonIgnoreProperties({"orders", "ratings", "interactions"})
  private ConsumerProfile consumer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  @JsonIgnoreProperties({"store", "consumer", "orderItems", "payments"})
  private Order order;

  // --- Rating Details ---

  @Column(name = "rating", nullable = false, precision = 3, scale = 2)
  private BigDecimal rating;

  @Column(name = "comment", length = 500)
  private String comment;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  // --- Validation Logic ---

  @PrePersist
  @PreUpdate
  public void validateRating() {
    if (rating == null
        || rating.compareTo(BigDecimal.valueOf(1)) < 0
        || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
      throw new IllegalArgumentException("Rating must be between 1.00 and 5.00");
    }
  }
}
