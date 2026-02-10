package com.frh.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ListingCategoryWeightDTO {
    private Long categoryId;
    private String categoryName;
    private BigDecimal weightKg;
}
