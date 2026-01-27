package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_profiles")
@Data
public class SupplierProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", unique = true, length = 30)
    private String phone;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "role", nullable = false, length = 20)
    private String role = "SUPPLIER";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- Supplier Specific Fields ---

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "uen_number")
    private String uenNumber;
}