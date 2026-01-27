package com.frh.backend.DTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StoreRequestDTO {
    private Long supplierId;
    private String storeName;
    private String addressLine;
    private String postalCode;

    // These come from Google Maps SDK
    private BigDecimal lat;
    private BigDecimal lng;

    private String openingHours;
    private String description;
}