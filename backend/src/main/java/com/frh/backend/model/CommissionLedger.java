package com.frh.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "commission_ledger")
@Data
public class CommissionLedger {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "commission_id", nullable = false)
  private Long commissionId;

  // --- Relationships ---

  // Each commission is tied to a specific Order.
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  // Each commission is also linked to the Supplier who "paid" it (via deduction).
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id", nullable = false)
  private SupplierProfile supplier;

  // --- Financial Details ---

  @Column(name = "commission_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal commissionAmount;

  // --- Status ---

  @Column(name = "status", nullable = false, length = 20)
  private String status = "DUE"; // Default to DUE

  // --- Timestamps ---

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
