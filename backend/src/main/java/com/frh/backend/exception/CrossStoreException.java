package com.frh.backend.exception;

public class CrossStoreException extends RuntimeException {

    private final Long currentSupplierId;

    public CrossStoreException(String message, Long currentSupplierId) {
        super(message);
        this.currentSupplierId = currentSupplierId;
    }

    public Long getCurrentSupplierId() {
        return currentSupplierId;
    }
}