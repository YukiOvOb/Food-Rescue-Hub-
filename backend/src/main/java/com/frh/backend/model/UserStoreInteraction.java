package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "user_store_interactions",
    indexes = {
      @Index(name = "idx_user_store_consumer_id", columnList = "consumer_id"),
      @Index(name = "idx_user_store_store_id", columnList = "store_id"),
      @Index(name = "idx_user_store_total_spend", columnList = "total_spend")
    })
@IdClass(UserStoreInteraction.UserStoreInteractionId.class)
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserStoreInteraction {

  @Id
  @Column(name = "consumer_id", nullable = false)
  private Long consumerId;

  @Id
  @Column(name = "store_id", nullable = false)
  private Long storeId;

  // --- Relationships ---

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "consumer_id", insertable = false, updatable = false)
  @JsonIgnoreProperties({"orders", "interactions", "ratings"})
  private ConsumerProfile consumer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", insertable = false, updatable = false)
  @JsonIgnoreProperties({"listings", "orders", "supplierProfile", "ratings"})
  private Store store;

  // --- Interaction Statistics ---

  @Column(name = "view_count", nullable = false)
  private Integer viewCount = 0;

  @Column(name = "click_count", nullable = false)
  private Integer clickCount = 0;

  @Column(name = "order_count", nullable = false)
  private Integer orderCount = 0;

  @Column(name = "total_spend", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalSpend = BigDecimal.ZERO;

  @Column(name = "last_order_at")
  private LocalDateTime lastOrderAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // --- Composite Primary Key Class ---

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserStoreInteractionId implements Serializable {
    private Long consumerId;
    private Long storeId;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      UserStoreInteractionId that = (UserStoreInteractionId) o;
      return Objects.equals(consumerId, that.consumerId) && Objects.equals(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(consumerId, storeId);
    }
  }
}
