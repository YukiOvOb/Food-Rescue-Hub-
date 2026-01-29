package com.frh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

//getters, setters, toString(), equals() can be automatically generated -> by using the lombok.Data
import lombok.Data;

@Data
public class RegisterRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;

  @NotBlank(message = "Display number is required")
  private String displayName;

  private String phone;

  private String businessName;
  private String businessType;
  private String payoutAccountRef;
}
