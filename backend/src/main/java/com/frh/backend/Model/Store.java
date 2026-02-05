package com.frh.backend.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stores")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Fixes lazy loading proxy errors
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    // --- Relationships ---

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @JsonIgnoreProperties("store") // Prevent Supplier -> Store -> Supplier loop
    private SupplierProfile supplierProfile;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("store") // Stop Listing from looking back at Store
    private List<Listing> listings;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("store")
    private List<Order> orders;

    // --- Data Fields ---

    @Column(name = "store_name", nullable = false, length = 200)
    private String storeName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "lat", precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(name = "lng", precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "pickup_instructions", length = 500)
    private String pickupInstructions;

    @Column(name = "opening_hours", length = 200)
    private String openingHours;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}