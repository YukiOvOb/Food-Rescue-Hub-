package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    // --- Relationship ---
    // Link back to the specific Order.
    // Using ManyToOne allows for retry attempts (e.g., Payment 1 fails, Payment 2 succeeds).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // --- Payment Details ---

    @Column(name = "provider", nullable = false, length = 40)
    private String provider; // e.g., "STRIPE", "PAYPAL", "DBS_PAYLAH"

    @Column(name = "provider_ref", length = 120)
    private String providerRef; // The transaction ID from the payment gateway

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // --- Status ---
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "INIT"; // Default to INIT

    // --- Timestamps ---

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
}
