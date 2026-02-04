package com.frh.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateLocationRequest {
    private BigDecimal latitude;
    private BigDecimal longitude;
}
