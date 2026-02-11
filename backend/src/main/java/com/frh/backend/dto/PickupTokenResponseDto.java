package com.frh.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PickupTokenResponseDto {
    private Long orderId;
    private String qrTokenHash;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
}
