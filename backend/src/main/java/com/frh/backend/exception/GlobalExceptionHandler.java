package com.frh.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        error.put("error", ex.getMessage());

        if (msg.contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        if (msg.contains("invalid email or password") || msg.contains("invalid credentials")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(CrossStoreException.class)
    public ResponseEntity<Map<String, Object>> handleCrossStoreException(CrossStoreException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.CONFLICT.value());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("currentSupplierId", ex.getCurrentSupplierId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}