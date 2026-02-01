package com.frh.backend.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;
// --- Relationships ---

    @JsonIgnore
    @OneToOne(mappedBy = "supplierProfile", cascade = CascadeType.ALL)
    private StoreType storeType;

    @JsonIgnore
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @JsonIgnore
    @Column(name = "phone", unique = true, length = 30)
    private String phone;

    @Column(name = "business_name", length = 200)
    private String businessName;

    @Column(name = "business_type", length = 80)
    private String businessType;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @JsonIgnore
    @Column(name = "payout_account_ref", length = 120)
    private String payoutAccountRef;

    @JsonIgnore
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @JsonIgnore
    @Column(name = "role", nullable = false, length = 20)
    private String role = "SUPPLIER";

    @JsonIgnore
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}