package com.frh.backend.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
   @JoinColumn(name = "supplier_id", referencedColumnName = "supplier_id", nullable = false, unique = true)
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
