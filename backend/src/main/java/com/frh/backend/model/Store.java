package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id", nullable = false)
  @JsonIgnoreProperties("stores") // Prevent Supplier -> Stores -> Supplier loop
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
