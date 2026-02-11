package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "listing_photos")
@Data
public class ListingPhoto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "listing_photo_id", nullable = false)
  private Long listingPhotoId;

  // --- Relationship ---
  // Many photos belong to one Listing
  // Inside ListingPhoto.java
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id")
  @JsonIgnore
  private Listing listing;

  // --- Photo Details ---

  @Column(name = "photo_url", nullable = false, length = 500)
  private String photoUrl;

  // Default value = 1 as per schema
  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 1;

  // --- Timestamp ---

  @CreationTimestamp
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private LocalDateTime uploadedAt;
}
