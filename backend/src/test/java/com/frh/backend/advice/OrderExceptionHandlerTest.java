package com.frh.backend.advice;

import com.frh.backend.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderExceptionHandlerTest {

    private final OrderExceptionHandler handler = new OrderExceptionHandler();

    @Test
    void handleInsufficientStock_returnsConflictWithMessage() {
        InsufficientStockException ex = new InsufficientStockException(9L, 5, 1);

        ResponseEntity<String> response = handler.handleInsufficientStock(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ex.getMessage(), response.getBody());
    }
}
