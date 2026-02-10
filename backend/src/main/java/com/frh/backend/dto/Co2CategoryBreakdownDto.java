package com.frh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Co2CategoryBreakdownDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalWeightKg;
    private BigDecimal totalCo2Kg;
}
