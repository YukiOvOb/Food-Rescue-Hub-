package com.frh.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter; // 换掉 Data
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@Entity
@Table(name = "listings")
@Getter // 只生成 Getter
@Setter // 只生成 Setter
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id")
    private Long listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // --- 关键修改：级联保存 Inventory ---
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "listing", cascade = CascadeType.ALL)
    private Inventory inventory;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "listing_dietary_tags",
            joinColumns = @JoinColumn(name = "listing_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<DietaryTag> dietaryTags = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingPhoto> photos = new ArrayList<>();

    // ... 其他字段保持不变 (Title, Price, Dates, etc) ...
    @Column(name = "title") private String title;
    @Column(name = "description") private String description;
    @Column(name = "original_price") private BigDecimal originalPrice;
    @Column(name = "rescue_price") private BigDecimal rescuePrice;
    @Column(name = "pickup_start") private LocalDateTime pickupStart;
    @Column(name = "pickup_end") private LocalDateTime pickupEnd;
    @Column(name = "expiry_at") private LocalDateTime expiryAt;
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}