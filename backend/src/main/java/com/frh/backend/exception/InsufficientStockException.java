package com.frh.backend.exception;

/**
 * Thrown when a consumer attempts to order more qty than is currently
 * available.
 * Maps to HTTP 409 Conflict via GlobalExceptionHandler.
 */
public class InsufficientStockException extends RuntimeException {

  private final Long listingId;
  private final int requested;
  private final int available;

  public InsufficientStockException(Long listingId, int requested, int available) {
    super("Insufficient stock for listing " + listingId +
        ": requested " + requested + ", available " + available);
    this.listingId = listingId;
    this.requested = requested;
    this.available = available;
  }

  public Long getListingId() {
    return listingId;
  }

  public int getRequested() {
    return requested;
  }

  public int getAvailable() {
    return available;
  }
}