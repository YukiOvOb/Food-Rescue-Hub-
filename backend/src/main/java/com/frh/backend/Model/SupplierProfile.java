package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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

    @OneToOne(mappedBy = "supplierProfile", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private StoreType storeType;

// --- Authentication Fields ---

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", unique = true, length = 30)
    private String phone;

    @Column(name = "business_name", length = 200)
    private String businessName;

    @Column(name = "business_type", length = 80)
    private String businessType;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(name = "payout_account_ref", length = 120)
    private String payoutAccountRef;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "role", nullable = false, length = 20)
    private String role = "SUPPLIER";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 