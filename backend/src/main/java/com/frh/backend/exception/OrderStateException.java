package com.frh.backend.exception;

/**
 * Thrown when an order action (accept / reject) is attempted
 * on an order that is not in the required state (PENDING).
 * Maps to HTTP 409 Conflict.
 */
public class OrderStateException extends RuntimeException {

  public OrderStateException(Long orderId, String currentStatus, String requiredStatus) {
    super("Order " + orderId + " is in state '" + currentStatus +
        "' but must be '" + requiredStatus + "' for this action");
  }
}