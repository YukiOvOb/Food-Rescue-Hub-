package com.frh.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // to automatically generate the default constructor without parameters
public class AuthResponse {
  private Long userId;
  private String email;
  private String displayName;
  private String role;
  private String message;

  // for the JWT token for authentication
  private String token;

  public AuthResponse(String token, Long userId, String email, String displayName, String role, String message) {
    this.token = token;
    this.userId = userId;
    this.email = email;
    this.displayName = displayName;
    this.role = role;
    this.message = message;
  }

  // Backward compatibility for existing frontend code using supplierId
  @JsonProperty("supplierId")
  public Long getSupplierId() {
    return userId;
  }

  @JsonProperty("supplierId")
  public void setSupplierId(Long supplierId) {
    this.userId = supplierId;
  }
}
