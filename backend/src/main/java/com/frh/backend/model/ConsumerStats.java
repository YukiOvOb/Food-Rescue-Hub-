package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "consumer_stats")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConsumerStats {

  @Id
  @Column(name = "consumer_id", nullable = false)
  private Long consumerId;

  // --- One-to-One Relationship (Shared Primary Key) ---

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "consumer_id")
  @JsonIgnoreProperties({"orders", "interactions", "ratings"})
  private ConsumerProfile consumer;

  // --- Order Statistics ---

  @Column(name = "total_orders", nullable = false)
  private Integer totalOrders = 0;

  @Column(name = "completed_orders", nullable = false)
  private Integer completedOrders = 0;

  @Column(name = "cancelled_orders", nullable = false)
  private Integer cancelledOrders = 0;

  // --- Spending Statistics ---

  @Column(name = "total_spend", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalSpend = BigDecimal.ZERO;

  @Column(name = "avg_order_value", precision = 10, scale = 2)
  private BigDecimal avgOrderValue;

  // --- Behavior Statistics ---

  @Column(name = "total_views", nullable = false)
  private Integer totalViews = 0;

  @Column(name = "total_clicks", nullable = false)
  private Integer totalClicks = 0;

  @Column(name = "favorite_store_type", length = 50)
  private String favoriteStoreType;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // --- Auto-Calculate Average Order Value ---

  @PrePersist
  @PreUpdate
  public void calculateAvgOrderValue() {
    if (completedOrders != null && completedOrders > 0 && totalSpend != null) {
      this.avgOrderValue =
          totalSpend.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP);
    } else {
      this.avgOrderValue = BigDecimal.ZERO;
    }
  }
}
