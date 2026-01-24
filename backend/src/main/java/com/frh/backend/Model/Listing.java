package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "listings")
@Data
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    // --- Relationship ---
    // Many listings belong to one Store.
    // referencing the 'store_id' column in this table.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "listing")
    private Inventory inventory;

    @ManyToMany(fetch = FetchType.LAZY) 
    @JoinTable(name = "listing_dietary_tags",
    joinColumns = @JoinColumn(name = "listing_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")) 
    private java.util.List<DietaryTag> dietaryTags;

    // --- Basic Details ---

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 800)
    private String description;

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
}
