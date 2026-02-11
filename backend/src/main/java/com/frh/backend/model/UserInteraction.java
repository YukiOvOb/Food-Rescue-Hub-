package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "user_interactions",
    indexes = {
      @Index(name = "idx_user_interaction_consumer_id", columnList = "consumer_id"),
      @Index(name = "idx_user_interaction_listing_id", columnList = "listing_id"),
      @Index(name = "idx_user_interaction_type", columnList = "interaction_type"),
      @Index(name = "idx_user_interaction_created_at", columnList = "created_at")
    })
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserInteraction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "interaction_id", nullable = false)
  private Long interactionId;

  // --- Relationships ---

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "consumer_id", nullable = false)
  @JsonIgnoreProperties({"orders", "interactions"})
  private ConsumerProfile consumer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id", nullable = false)
  @JsonIgnoreProperties({"store", "inventory", "dietaryTags", "photos"})
  private Listing listing;

  // --- Interaction Details ---

  @Enumerated(EnumType.STRING)
  @Column(name = "interaction_type", nullable = false, length = 20)
  private InteractionType interactionType;

  @Column(name = "session_id", length = 100)
  private String sessionId;

  @Column(name = "device_type", length = 20)
  private String deviceType;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  // --- Enum for Interaction Types ---

  public enum InteractionType {
    VIEW,
    CLICK,
    SEARCH,
    ADD_TO_CART
  }
}
