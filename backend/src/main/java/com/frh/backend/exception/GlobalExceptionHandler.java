package com.frh.backend.exception;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
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

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return buildError(status, message);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        ConversionFailedException.class,
        MissingServletRequestPartException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentNotValidException.class,
        BindException.class,
        ServletRequestBindingException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMostSpecificCause() != null
            ? ex.getMostSpecificCause().getMessage()
            : ex.getMessage());
    }

    // Generic runtime fallback for uncaught domain errors.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (msg.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (msg.contains("invalid email or password")
            || msg.contains("invalid credentials")
            || msg.contains("unauthorized")
            || msg.contains("not authorised")
            || msg.contains("not authenticated")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (msg.contains("forbidden") || msg.contains("only consumers")) {
            status = HttpStatus.FORBIDDEN;
        } else if (msg.contains("already registered")
            || msg.contains("already exists")
            || msg.contains("duplicate")) {
            status = HttpStatus.CONFLICT;
        }
        return buildError(status, ex.getMessage());
    }

    @ExceptionHandler(CrossStoreException.class)
    public ResponseEntity<Map<String, Object>> handleCrossStoreException(CrossStoreException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.CONFLICT.value());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("currentSupplierId", ex.getCurrentSupplierId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticFailure(ObjectOptimisticLockingFailureException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "CONCURRENT_MODIFICATION");
        body.put("message", "Concurrent modification detected. Please retry the operation.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private ResponseEntity<Map<String, String>> buildError(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        String safeMessage = (message == null || message.isBlank()) ? status.getReasonPhrase() : message;
        body.put("error", safeMessage);
        return ResponseEntity.status(status).body(body);
    }
}
