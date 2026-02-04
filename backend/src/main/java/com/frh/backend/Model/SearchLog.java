package com.frh.backend.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_logs", indexes = {
    @Index(name = "idx_consumer_id", columnList = "consumer_id"),
    @Index(name = "idx_query_text", columnList = "query_text"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id", nullable = false)
    private Long searchId;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    @JsonIgnoreProperties({"orders", "interactions", "ratings"})
    private ConsumerProfile consumer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clicked_listing_id")
    @JsonIgnoreProperties({"store", "inventory", "dietaryTags", "photos"})
    private Listing clickedListing;

    // --- Search Details ---

    @Column(name = "query_text", nullable = false, length = 200)
    private String queryText;

    @Column(name = "results_count")
    private Integer resultsCount;

    @Column(name = "click_position")
    private Integer clickPosition;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
