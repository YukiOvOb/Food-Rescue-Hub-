package com.frh.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "listing_food_categories")
@Getter
@Setter
public class ListingFoodCategory {

  @EmbeddedId private ListingFoodCategoryId id = new ListingFoodCategoryId();

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("listingId")
  @JoinColumn(name = "listing_id", nullable = false)
  private Listing listing;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("categoryId")
  @JoinColumn(name = "category_id", nullable = false)
  private FoodCategory category;

  @Column(name = "weight_kg", precision = 10, scale = 3)
  private BigDecimal weightKg;
}
