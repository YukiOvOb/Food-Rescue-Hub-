package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "consumer_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerProfile {

    @Id
    @Column(name = "consumer_id")
    private Long consumerId;

    /**
     * Shared Primary Key:
     * This links to the User entity. @MapsId tells Hibernate that the 
     * 'consumerId' of this class is derived from the 'userId' of the User.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "consumer_id")
    private User user;

    /**
     * JSON columns are typically stored as Strings in standard JPA.
     * To map this to a specific Java Object or Map automatically, 
     * you would need a custom AttributeConverter or a library like 'hypersistence-utils'.
     */
    @Column(name = "preferences_json", columnDefinition = "json")
    private String preferencesJson;

    // DECIMAL(10,7) maps best to BigDecimal in Java for precision
    @Column(name = "default_lat", precision = 10, scale = 7)
    private BigDecimal defaultLat;

    @Column(name = "default_lng", precision = 10, scale = 7)
    private BigDecimal defaultLng;
}
