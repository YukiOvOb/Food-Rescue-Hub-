package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "listings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class Listing {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "listing_id", nullable = false)
  private Long listingId;

  // --- Relationship ---
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  @JsonIgnoreProperties({"listings", "orders", "supplierProfile"}) // Breaks the loop here
  private Store store;

  @JsonIgnore
  @OneToOne(fetch = FetchType.LAZY, mappedBy = "listing", cascade = CascadeType.ALL)
  private Inventory inventory;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "listing_dietary_tags",
      joinColumns = @JoinColumn(name = "listing_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private java.util.List<DietaryTag> dietaryTags;

  @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ListingPhoto> photos = new ArrayList<>();

  @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ListingFoodCategory> listingFoodCategories = new ArrayList<>();

  // --- Basic Details ---

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "description", length = 800)
  private String description;

  @Column(name = "estimated_weight_kg", precision = 10, scale = 3)
  private BigDecimal estimatedWeightKg;

  // --- Pricing ---
  // Using BigDecimal for currency is best practice to avoid floating point errors.
  // Precision (10,2) matches the DB schema.
  @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal originalPrice;

  @Column(name = "rescue_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal rescuePrice;

  // --- Timing ---

  @Column(name = "pickup_start", nullable = false)
  private LocalDateTime pickupStart;

  @Column(name = "pickup_end", nullable = false)
  private LocalDateTime pickupEnd;

  @Column(name = "expiry_at", nullable = false)
  private LocalDateTime expiryAt;

  private String status = "ACTIVE"; // Default to ACTIVE

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public int getAvailableQty() {
    return inventory != null && inventory.getQtyAvailable() != null
        ? inventory.getQtyAvailable()
        : 0;
  }

  public void setAvailableQty(int availableQty) {
    if (inventory == null) {
      inventory = new Inventory();
      inventory.setListing(this);
    }
    inventory.setQtyAvailable(availableQty);
  }
}
