package com.frh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {
    private Long storeId;
    private Long supplierId; // Just the ID, not the whole object!
    private String storeName;
    private String addressLine;
    private String postalCode;
    private BigDecimal lat;
    private BigDecimal lng;
    private String openingHours;
    private String description;
    private boolean active;
}