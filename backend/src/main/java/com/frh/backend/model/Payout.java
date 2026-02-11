package com.frh.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "payouts")
@Data
public class Payout {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payout_id", nullable = false)
  private Long payoutId;

  // --- Relationship ---
  // Many payouts belong to one Supplier
  // Assuming you have a SupplierProfile entity
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id", nullable = false)
  private SupplierProfile supplierProfile;

  // --- Period Details ---

  @Column(name = "period_start", nullable = false)
  private LocalDateTime periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDateTime periodEnd;

  // --- Financials ---
  // Using BigDecimal for all money columns (Precision 10,2)

  @Column(name = "amount_gross", nullable = false, precision = 10, scale = 2)
  private BigDecimal amountGross;

  @Column(name = "commission_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal commissionAmount;

  @Column(name = "amount_net", nullable = false, precision = 10, scale = 2)
  private BigDecimal amountNet;

  // --- Status ---
  @Column(name = "status", nullable = false, length = 20)
  private String status = "PENDING"; // Default

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
