package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
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

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "supplier_id")
    private User user;


    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "business_type", length = 80)
    private String businessType;

    @Column(name = "payout_account_ref", length = 120)
    private String payoutAccountRef;

    @Column(name = "verification_status", nullable = false, length = 20)
    private String verificationStatus = "PENDING";



    @Column(name = "role", nullable = false, length = 20)
    private String role = "SUPPLIER";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;


    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "uen_number", length = 255)
    private String uenNumber;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}