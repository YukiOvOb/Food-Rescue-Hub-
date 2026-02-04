package com.frh.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*Sent by the consumer (or mobile app) when placing a new order.*/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

  @NotNull(message = "listingId is required")
  private Long listingId;

  @NotNull(message = "consumerId is required")
  private Long consumerId;

  @NotNull(message = "quantity is required")
  @Min(value = 1, message = "quantity must be at least 1")
  private Integer quantity;

  // optional- consumer may pick a slot inside the listing's pickup window
  private LocalDateTime pickupSlotStart;
  private LocalDateTime pickupSlotEnd;
}