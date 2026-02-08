package com.frh.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleInsufficientStock_returnsConflictBody() {
        InsufficientStockException ex = new InsufficientStockException(10L, 4, 2);

        ResponseEntity<Map<String, Object>> response = handler.handleInsufficientStock(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("INSUFFICIENT_STOCK", response.getBody().get("error"));
        assertEquals(10L, response.getBody().get("listingId"));
        assertEquals(4, response.getBody().get("requested"));
        assertEquals(2, response.getBody().get("available"));
    }

    @Test
    void handleOrderState_returnsConflictBody() {
        OrderStateException ex = new OrderStateException(1L, "ACCEPTED", "PENDING");

        ResponseEntity<Map<String, String>> response = handler.handleOrderState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("ORDER_STATE_CONFLICT", response.getBody().get("error"));
    }

    @Test
    void handleRuntimeException_notFound() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException("Listing not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleRuntimeException_unauthorized() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException("Invalid email or password"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleRuntimeException_internalServerError() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException("Unexpected"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected", response.getBody().get("error"));
    }

    @Test
    void handleCrossStoreException_returnsConflictBody() {
        CrossStoreException ex = new CrossStoreException("Cross store", 99L);

        ResponseEntity<Map<String, Object>> response = handler.handleCrossStoreException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().get("code"));
        assertEquals("Cross store", response.getBody().get("message"));
        assertEquals(99L, response.getBody().get("currentSupplierId"));
    }

    @Test
    void handleOptimisticFailure_returnsConflictBody() {
        ObjectOptimisticLockingFailureException ex =
            new ObjectOptimisticLockingFailureException("Order", 1L, new OptimisticLockingFailureException("conflict"));

        ResponseEntity<Map<String, String>> response = handler.handleOptimisticFailure(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONCURRENT_MODIFICATION", response.getBody().get("error"));
    }
}
