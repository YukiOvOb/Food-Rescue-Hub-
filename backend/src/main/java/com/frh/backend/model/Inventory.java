package com.frh.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inventory_id", nullable = false)
  private Long inventoryId;

  // --- Relationship ---
  // Assuming 1 Listing has 1 Inventory record.
  // If your logic allows multiple inventory batches per listing, change this to @ManyToOne.
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id", nullable = false)
  private Listing listing;

  // --- Stock Levels ---

  @Column(name = "qty_available", nullable = false)
  private Integer qtyAvailable;

  @Column(name = "qty_reserved", nullable = false)
  private Integer qtyReserved = 0; // Default to 0 in Java to match DB default

  // --- Timestamp ---

  // @UpdateTimestamp will automatically update this field whenever the entity is modified.
  // This is perfect for inventory tracking where you want to know when the stock last changed.
  @UpdateTimestamp
  @Column(name = "last_updated", nullable = false)
  private LocalDateTime lastUpdated;
}
