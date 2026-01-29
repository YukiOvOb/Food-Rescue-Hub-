package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "consumer_profiles")
@Data
public class ConsumerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumer_id", nullable = false)
    private Long consumerId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", unique = true, length = 30)
    private String phone;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "role", nullable = false, length = 20)
    private String role = "CONSUMER";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "default_lat", precision = 10, scale = 7)
    private BigDecimal default_lat;

    @Column(name = "default_lng", precision = 10, scale = 7)
    private BigDecimal default_lng;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences_json", columnDefinition = "json")
    private Map<String, Object> preferences;

    // --- Relationships ---
    @OneToOne(mappedBy = "consumer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
