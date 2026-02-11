package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "store_stats")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StoreStats {

  @Id
  @Column(name = "store_id", nullable = false)
  private Long storeId;

  // --- One-to-One Relationship (Shared Primary Key) ---

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "store_id")
  @JsonIgnoreProperties({"listings", "orders", "supplierProfile", "ratings"})
  private Store store;

  // --- Rating Statistics ---

  @Column(name = "avg_rating", precision = 3, scale = 2)
  private BigDecimal avgRating;

  @Column(name = "total_ratings", nullable = false)
  private Integer totalRatings = 0;

  // --- Order Statistics ---

  @Column(name = "total_orders", nullable = false)
  private Integer totalOrders = 0;

  @Column(name = "completed_orders", nullable = false)
  private Integer completedOrders = 0;

  @Column(name = "completion_rate", precision = 5, scale = 4)
  private BigDecimal completionRate;

  @Column(name = "on_time_deliveries", nullable = false)
  private Integer onTimeDeliveries = 0;

  @Column(name = "on_time_rate", precision = 5, scale = 4)
  private BigDecimal onTimeRate;

  // --- Listing Statistics ---

  @Column(name = "active_listings", nullable = false)
  private Integer activeListings = 0;

  @Column(name = "total_views", nullable = false)
  private Integer totalViews = 0;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
