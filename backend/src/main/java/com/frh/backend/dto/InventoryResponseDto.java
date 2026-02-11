package com.frh.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryResponseDto {
    private Long inventoryId;
    private Long listingId;
    private Integer qtyAvailable;
    private Integer qtyReserved;
    private LocalDateTime lastUpdated;
}
