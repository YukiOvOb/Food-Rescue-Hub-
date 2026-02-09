package com.frh.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

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
    void handleRuntimeException_internalServerErrorFallback() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException("Unexpected"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected", response.getBody().get("error"));
    }

    @Test
    void handleRuntimeException_forbidden() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException("Only consumers can checkout"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleRuntimeException_conflict() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException("Email already exists"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleRuntimeException_unauthorized_withNotAuthorisedMessage() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException("User not authorised"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleRuntimeException_conflict_withDuplicateMessage() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException("Duplicate account"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleRuntimeException_blankMessage_usesReasonPhrase() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException("   "));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().get("error"));
    }

    @Test
    void handleRuntimeException_nullMessage_usesReasonPhrase() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(new RuntimeException((String) null));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().get("error"));
    }

    @Test
    void handleResponseStatusException_propagatesStatus() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");

        ResponseEntity<Map<String, String>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not authenticated", response.getBody().get("error"));
    }

    @Test
    void handleResponseStatusException_unknownStatus_defaultsToBadRequest() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatusCode.valueOf(499), null);

        ResponseEntity<Map<String, String>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ex.getMessage(), response.getBody().get("error"));
    }

    @Test
    void handleBadRequest_returnsBadRequest() {
        ResponseEntity<Map<String, String>> response =
            handler.handleBadRequest(new HttpMessageNotReadableException("Malformed JSON"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed JSON", response.getBody().get("error"));
    }

    @Test
    void handleBadRequest_withTypeMismatchException() {
        ConversionFailedException conversionFailedException = new ConversionFailedException(
            TypeDescriptor.valueOf(String.class),
            TypeDescriptor.valueOf(Long.class),
            "abc",
            new IllegalArgumentException("invalid number")
        );

        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(conversionFailedException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(conversionFailedException.getMessage(), response.getBody().get("error"));
    }

    @Test
    void handleBadRequest_withMissingParameterException() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException("storeId", "Long");

        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(exception.getMessage(), response.getBody().get("error"));
    }

    @Test
    void handleDataIntegrity_usesMostSpecificCauseMessage() {
        DataIntegrityViolationException exception =
            new DataIntegrityViolationException("conflict", new RuntimeException("duplicate key value"));

        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrity(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("duplicate key value", response.getBody().get("error"));
    }

    @Test
    void handleDataIntegrity_fallsBackToExceptionMessageWhenCauseMissing() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("conflict fallback") {
            @Override
            public Throwable getMostSpecificCause() {
                return null;
            }
        };

        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrity(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("conflict fallback", response.getBody().get("error"));
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
