package com.frh.backend.Model;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pickup_tokens")
@Data
public class PickupToken {

    // --- Shared Primary Key ---
    // We do NOT use @GeneratedValue here. 
    // The ID is manually assigned from the linked Order object.
    @Id
    @Column(name = "order_id")
    private Long orderId;

    // --- Relationship ---
    // @MapsId tells Hibernate: "Use the ID from this 'order' object as my Primary Key"
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId 
    @JoinColumn(name = "order_id")
    private Order order;

    // --- Token Security ---

    @Column(name = "qr_token_hash", nullable = false, length = 255)
    private String qrTokenHash;

    // --- Timestamps ---

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // --- Helper Method ---
    // Check if token is valid for scanning
    public boolean isValid() {
        return usedAt == null && LocalDateTime.now().isBefore(expiresAt);
    }
}
