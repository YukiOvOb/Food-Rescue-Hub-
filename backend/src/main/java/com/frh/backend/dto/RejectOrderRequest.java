package com.frh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* Body sent when a supplier rejects a pending order.
 * The reason is stored in {@code orders.cancel_reason}.*/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectOrderRequest {

  @NotBlank(message = "A rejection reason is required")
  @Size(max = 300, message = "Reason must be ≤ 300 characters")
  private String reason;
}
