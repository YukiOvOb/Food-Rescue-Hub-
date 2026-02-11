package com.frh.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InventoryResponseDto {
  private Long inventoryId;
  private Long listingId;
  private Integer qtyAvailable;
  private Integer qtyReserved;
  private LocalDateTime lastUpdated;
}
