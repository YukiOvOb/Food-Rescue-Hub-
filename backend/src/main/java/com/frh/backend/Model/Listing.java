package com.frh.backend.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    // --- Relationship ---
    @JsonIgnore // avoid lazy serialization loops
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "listing", cascade = CascadeType.ALL)
    private Inventory inventory;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "listing_dietary_tags",
            joinColumns = @JoinColumn(name = "listing_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<DietaryTag> dietaryTags;

    @JsonIgnore
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingPhoto> photos = new ArrayList<>();

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
