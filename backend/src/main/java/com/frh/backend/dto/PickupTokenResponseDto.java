package com.frh.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PickupTokenResponseDto {
  private Long orderId;
  private String qrTokenHash;
  private LocalDateTime issuedAt;
  private LocalDateTime expiresAt;
  private LocalDateTime usedAt;
}
