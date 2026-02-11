package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "cart")
@Getter
@Setter
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cart_id", nullable = false)
  private Long cartId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "consumer_id", nullable = false)
  @JsonIgnore
  private ConsumerProfile consumer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id")
  @JsonIgnore
  private Store store;

  @Column(name = "status", nullable = false, length = 30)
  private String status = "ACTIVE";

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
