package com.frh.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.frh.backend.exception.InsufficientStockException;
import com.frh.backend.exception.OrderStateException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 409 Insufficient stock (oversell guard)
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "INSUFFICIENT_STOCK");
        body.put("message", ex.getMessage());
        body.put("listingId", ex.getListingId());
        body.put("requested", ex.getRequested());
        body.put("available", ex.getAvailable());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 409 Bad order-state transition
    @ExceptionHandler(OrderStateException.class)
    public ResponseEntity<Map<String, String>> handleOrderState(OrderStateException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "ORDER_STATE_CONFLICT");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 404 / 500 -Generic runtime
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