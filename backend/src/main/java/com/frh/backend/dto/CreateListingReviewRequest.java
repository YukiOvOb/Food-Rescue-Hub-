package com.frh.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateListingReviewRequest {

  @NotNull(message = "orderId is required")
  private Long orderId;

  @NotNull(message = "listingId is required")
  private Long listingId;

  @NotNull(message = "storeRating is required")
  @Min(value = 1, message = "storeRating must be at least 1")
  @Max(value = 5, message = "storeRating must be at most 5")
  private Integer storeRating;

  @NotNull(message = "listingAccuracy is required")
  @Min(value = 1, message = "listingAccuracy must be at least 1")
  @Max(value = 5, message = "listingAccuracy must be at most 5")
  private Integer listingAccuracy;

  @NotNull(message = "onTimePickup is required")
  @Min(value = 1, message = "onTimePickup must be at least 1")
  @Max(value = 5, message = "onTimePickup must be at most 5")
  private Integer onTimePickup;

  @NotBlank(message = "comment is required")
  @Size(max = 300, message = "comment must be 300 characters or less")
  private String comment;
}
