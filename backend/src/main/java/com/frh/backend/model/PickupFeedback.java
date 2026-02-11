package com.frh.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "pickup_feedback")
@Data
public class PickupFeedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "feedback_id", nullable = false)
  private Long feedbackId;

  // --- Relationship ---
  // One Order = One Feedback.
  // We map this as OneToOne so we can access order details easily.
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  // --- Feedback Details ---

  @Column(name = "feedback_review", length = 300)
  private String feedbackReview;

  // Using wrapper class 'Boolean' because schema doesn't say "NOT NULL"
  // This allows it to be null if the user hasn't rated "on time" yet.
  @Column(name = "on_time")
  private Boolean onTime;

  // --- Timestamps ---

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "used_at")
  private LocalDateTime usedAt;
}
