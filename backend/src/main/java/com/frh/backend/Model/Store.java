package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stores")
@Data
public class Store {

    // --- Primary Key ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Assumes Auto-Increment in DB
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    // --- Foreign Key Column ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierProfile supplierProfile;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @OneToOne(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Listing listing;

    // --- Data Fields ---

    @Column(name = "store_name", nullable = false, length = 200)
    private String storeName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    // using BigDecimal for precision (10,7)
    @Column(name = "lat", precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(name = "lng", precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "pickup_instructions", length = 500)
    private String pickupInstructions;

    @Column(name = "opening_hours", length = 200)
    private String openingHours;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Default to true

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
