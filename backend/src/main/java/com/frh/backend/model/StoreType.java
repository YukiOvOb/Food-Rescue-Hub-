package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "store_types")
@Data
public class StoreType {

  // --- Primary Key ---
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "type_id", nullable = false)
  private Long typeId;

  // --- Foreign Key ---
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(
      name = "supplier_id",
      referencedColumnName = "supplier_id",
      nullable = false,
      unique = true)
  @JsonIgnore
  private SupplierProfile supplierProfile;

  // --- Data Fields ---

  @Column(name = "type_name", nullable = false, length = 200)
  private String typeName;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true; // Default value as per schema

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
