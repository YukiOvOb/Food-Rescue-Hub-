package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "admin_profiles")
@Data
public class AdminProfile {

    // --- Shared Primary Key ---
    // The admin_id IS the user_id.
    @Id
    @Column(name = "admin_id")
    private Long adminId;

    // --- Relationship ---
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "admin_id")
    private User user;
}
