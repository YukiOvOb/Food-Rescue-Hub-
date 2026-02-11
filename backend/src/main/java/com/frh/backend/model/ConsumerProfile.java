package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "consumer_profiles")
@Data
public class ConsumerProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "consumer_id", nullable = false)
  private Long consumerId;

  @JsonIgnore
  @Column(name = "email", nullable = false, unique = true, length = 255)
  private String email;

  @JsonIgnore
  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @JsonIgnore
  @Column(name = "phone", unique = true, length = 30)
  private String phone;

  @Column(name = "display_name", length = 120)
  private String displayName;

  @JsonIgnore
  @Column(name = "status", nullable = false, length = 20)
  private String status = "ACTIVE";

  @JsonIgnore
  @Column(name = "role", nullable = false, length = 20)
  private String role = "CONSUMER";

  @JsonIgnore
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "default_lat", precision = 10, scale = 7)
  private BigDecimal default_lat;

  @Column(name = "default_lng", precision = 10, scale = 7)
  private BigDecimal default_lng;

  @JsonIgnore
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "preferences_json", columnDefinition = "json")
  private Map<String, Object> preferences;

  // --- Relationships ---
  @JsonIgnoreProperties({"consumer", "transactions"})
  @OneToOne(mappedBy = "consumer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Wallet wallet;
}
