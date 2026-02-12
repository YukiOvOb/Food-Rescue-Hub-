package com.frh.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * Data Transfer Object for Listing information Used to send listing data to the client with all
 * necessary details
 */
@Data
public class ListingDTO {

  private Long listingId;
  private String title;
  private String description;
  private BigDecimal originalPrice;
  private BigDecimal rescuePrice;
  private LocalDateTime pickupStart;
  private LocalDateTime pickupEnd;
  private LocalDateTime expiryAt;
  private String status;

  // Store information
  private Long storeId;
  private String storeName;
  private String storeDescription;
  private String addressLine;
  private String postalCode;
  private BigDecimal lat;
  private BigDecimal lng;
  private String pickupInstructions;
  private String openingHours;

  // Store type/category
  private String category;

  // CO2 TRACKING
  private List<Long> categoryIds;
  private List<String> categoryNames;
  private List<ListingCategoryWeightDTO> categoryWeights;
  private BigDecimal estimatedWeightKg;

  // Inventory
  private Integer qtyAvailable;
  private Integer qtyReserved;

  // Photos
  private List<String> photoUrls;

  // Calculated fields
  private String timeRemaining; // e.g., "2h 15m left"
  private BigDecimal savingsAmount; // originalPrice - rescuePrice
  private String savingsLabel; // e.g., "Worth $12+"

  // Review ratings (percentages)
  private Double avgListingAccuracy; // Average listing accuracy rating as percentage
  private Double avgOnTimePickup; // Average on-time pickup rating as percentage
}
