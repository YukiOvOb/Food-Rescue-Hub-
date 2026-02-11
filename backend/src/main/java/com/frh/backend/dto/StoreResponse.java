package com.frh.backend.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {
  private Long storeId;
  private Long supplierId;
  private String storeName;
  private String addressLine;
  private String postalCode;
  private BigDecimal lat;
  private BigDecimal lng;
  private String openingHours;
  private String description;
  private String pickupInstructions;
  private boolean active;
}
