package com.frh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {

  @NotNull(message = "Supplier ID cannot be null")
  @Schema(description = "ID of the user/supplier creating the store", example = "5")
  private Long supplierId;

  @NotBlank(message = "Store name is required")
  @Size(min = 3, max = 100, message = "Store name must be between 3 and 100 characters")
  @Schema(description = "Public name of the store", example = "Bakery")
  private String storeName;

  @NotBlank(message = "Address is required")
  @Schema(example = "123 NUS Road")
  private String addressLine;

  @Pattern(regexp = "^\\d{6}$", message = "Postal code must be exactly 6 digits")
  @Schema(example = "119077")
  private String postalCode;

  // --- Google Maps Coordinates (Singapore Only) ---

  @NotNull(message = "Latitude is required")
  @DecimalMin(value = "1.15", message = "Latitude must be within Singapore (min 1.15)")
  @DecimalMax(value = "1.48", message = "Latitude must be within Singapore (max 1.48)")
  @Schema(example = "1.3521") // Center of Singapore
  private BigDecimal lat;

  @NotNull(message = "Longitude is required")
  @DecimalMin(value = "103.59", message = "Longitude must be within Singapore (min 103.59)")
  @DecimalMax(value = "104.05", message = "Longitude must be within Singapore (max 104.05)")
  @Schema(example = "103.8198")
  private BigDecimal lng;

  @Size(max = 50, message = "Opening hours text is too long")
  @Schema(example = "09:00 - 21:00")
  private String openingHours;

  @Size(max = 500, message = "Description cannot exceed 500 characters")
  private String description;

  private String pickupInstructions;
}
