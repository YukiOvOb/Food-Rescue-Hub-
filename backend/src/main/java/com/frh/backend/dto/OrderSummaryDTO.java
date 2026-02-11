package com.frh.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Read-only the DTO returned to the supplier's order-queue UI. Contains every piece of information
 * the supplier needs at a glance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {

  // Order identity
  private Long orderId;
  private String status; // PENDING/ACCEPTED/REJECTED/COMPLETED/CANCELLED

  // What was ordered
  private Long listingId;
  private String listingTitle;
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal totalAmount;
  private String currency;

  // Consumer info (so supplier knows who placed it)
  private Long consumerId;
  private String consumerName;
  private String consumerPhone;
  private ConsumerInfo consumer;
  private String pickupTokenHash;
  private LocalDateTime pickupTokenExpiresAt;

  // Store info
  private StoreInfo store;

  // Timing
  private LocalDateTime pickupSlotStart;
  private LocalDateTime pickupSlotEnd;
  private LocalDateTime createdAt;

  // Cancellation (populated only when status == REJECTED)
  private String cancelReason;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StoreInfo {
    private Long storeId;
    private String storeName;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ConsumerInfo {
    private Long consumerId;
    private String displayName;
    private String username;
  }
}
