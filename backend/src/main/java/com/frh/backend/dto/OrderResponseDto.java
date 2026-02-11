package com.frh.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponseDto {
  private Long orderId;
  private String status;
  private BigDecimal totalAmount;
  private String currency;
  private LocalDateTime pickupSlotStart;
  private LocalDateTime pickupSlotEnd;
  private String cancelReason;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private StoreDto store;
  private ConsumerDto consumer;
  private List<OrderItemDto> orderItems = new ArrayList<>();

  private String pickupTokenHash;
  private LocalDateTime pickupTokenExpiresAt;

  @Data
  public static class StoreDto {
    private Long storeId;
    private String storeName;
    private String addressLine;
    private String postalCode;
    private BigDecimal lat;
    private BigDecimal lng;
  }

  @Data
  public static class ConsumerDto {
    private Long consumerId;
    private String displayName;

    @JsonProperty("default_lat")
    private BigDecimal defaultLat;

    @JsonProperty("default_lng")
    private BigDecimal defaultLng;
  }

  @Data
  public static class OrderItemDto {
    private Long orderItemId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private ListingDto listing;
  }

  @Data
  public static class ListingDto {
    private Long listingId;
    private String title;
  }
}
