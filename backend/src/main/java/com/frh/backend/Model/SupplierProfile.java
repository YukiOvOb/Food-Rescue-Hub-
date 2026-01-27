package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProfile {

    @Id
    @Column(name = "supplier_id")
    private Long supplierId;

    /**
     * Shared Primary Key with User.
     * The @MapsId annotation links 'supplierId' to the 'userId' of the User entity.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "supplier_id")
    private User user;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "business_type", length = 80)
    private String businessType;

    @Column(name = "verification_status", nullable = false, length = 20)
    private String verificationStatus = "PENDING"; // Set default

    @Column(name = "payout_account_ref", length = 120)
    private String payoutAccountRef;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = true)
    private LocalDateTime updatedAt;
}