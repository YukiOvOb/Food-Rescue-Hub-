package com.frh.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_item_id", nullable = false)
  private Long orderItemId;

  // --- Relationships ---

  // Link back to the parent Order
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  // Link to the specific Product/Listing purchased
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id", nullable = false)
  private Listing listing;

  // --- Item Details ---

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  // Using BigDecimal for currency (Precision 10,2)
  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
  private BigDecimal lineTotal;

  // --- Optional Helper Method ---
  // This ensures line_total is always calculated correctly before saving.
  @PrePersist
  @PreUpdate
  public void calculateTotal() {
    if (this.unitPrice != null && this.quantity != null) {
      this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
  }
}
