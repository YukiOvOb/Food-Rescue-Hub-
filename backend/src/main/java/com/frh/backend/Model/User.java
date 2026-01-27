package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data // Generates Getters, Setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(unique = true, length = 255)
    private String email;

    @Column(unique = true, length = 30)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // Default value handling

    @CreationTimestamp // Automatically sets timestamp when saving
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- Relationship ---
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY) 
    private ConsumerProfile consumerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SupplierProfile supplierProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY) 
    private AdminProfile adminProfile;
}
