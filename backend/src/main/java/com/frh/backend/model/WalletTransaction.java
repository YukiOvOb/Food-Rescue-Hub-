package com.frh.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "wallet_transactions")
@Data
public class WalletTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "wallet_txn_id", nullable = false)
  private Long walletTxnId;

  // --- Relationship ---
  // Link back to the parent Wallet
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet;

  // --- Transaction Details ---

  @Column(name = "txn_type", nullable = false, length = 30)
  private String txnType; // e.g., TOPUP, PAYMENT

  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(name = "reference_id")
  private String referenceId; // Useful to link to an Order ID or Payment Gateway ID

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
