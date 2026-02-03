package com.frh.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long listingId, int requested, int available) {
        super("Insufficient stock for listing " + listingId + ". Requested=" + requested + ", Available=" + available);
    }
}