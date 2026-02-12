package com.frh.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ListingReviewResponse {

  private Long reviewId;
  private Long orderId;
  private Long listingId;
  private String listingTitle;
  private Integer storeRating;
  private Integer listingAccuracy;
  private Integer onTimePickup;
  private String comment;
  private LocalDateTime createdAt;
  private Long consumerId;
  private String consumerDisplayName;
}
