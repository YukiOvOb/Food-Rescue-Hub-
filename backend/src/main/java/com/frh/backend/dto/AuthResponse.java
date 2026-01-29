package com.frh.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // to automatically generate the default constructor without parameters
public class AuthResponse {
  private Long supplierId;
  private String email;
  private String displayName;
  private String role;
  private String message;

  // for the JWT token for authentication
  private String token;

  public AuthResponse(String token, Long supplierId, String email, String displayName, String role, String message) {
    this.token = token;
    this.supplierId = supplierId;
    this.email = email;
    this.displayName = displayName;
    this.role = role;
    this.message = message;
  }
}
