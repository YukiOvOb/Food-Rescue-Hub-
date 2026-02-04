package com.frh.backend.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "listing_stats")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ListingStats {

    @Id
    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    // --- One-to-One Relationship (Shared Primary Key) ---

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "listing_id")
    @JsonIgnoreProperties({"store", "inventory", "dietaryTags", "photos"})
    private Listing listing;

    // --- Statistics ---

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;

    @Column(name = "add_to_cart_count", nullable = false)
    private Integer addToCartCount = 0;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    // --- Calculated Metrics ---

    @Column(name = "ctr", precision = 5, scale = 4)
    private BigDecimal ctr; // Click-Through Rate = click_count / view_count

    @Column(name = "cvr", precision = 5, scale = 4)
    private BigDecimal cvr; // Conversion Rate = order_count / view_count

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Auto-Calculate CTR and CVR ---

    @PrePersist
    @PreUpdate
    public void calculateMetrics() {
        // Calculate CTR
        if (viewCount != null && viewCount > 0) {
            this.ctr = BigDecimal.valueOf(clickCount)
                .divide(BigDecimal.valueOf(viewCount), 4, RoundingMode.HALF_UP);
        } else {
            this.ctr = BigDecimal.ZERO;
        }

        // Calculate CVR
        if (viewCount != null && viewCount > 0) {
            this.cvr = BigDecimal.valueOf(orderCount)
                .divide(BigDecimal.valueOf(viewCount), 4, RoundingMode.HALF_UP);
        } else {
            this.cvr = BigDecimal.ZERO;
        }
    }
}
