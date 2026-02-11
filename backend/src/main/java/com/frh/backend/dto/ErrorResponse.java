package com.frh.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data

// this AllargsConstructor is used mainly to create the constructor
// automatically by using the fields as parameters
@AllArgsConstructor
public class ErrorResponse {

  private LocalDateTime timestamp;
  private int status;
  private String message;
  private Map<String, String> errors;

  public ErrorResponse(int status, String message) {
    this.timestamp = LocalDateTime.now();
    this.status = status;
    this.message = message;
  }
}
