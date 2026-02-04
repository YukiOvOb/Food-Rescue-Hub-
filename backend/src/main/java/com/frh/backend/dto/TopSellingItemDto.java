package com.frh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopSellingItemDto {
    private Long listingId;
    private String title;
    private Long totalQuantity;
}
