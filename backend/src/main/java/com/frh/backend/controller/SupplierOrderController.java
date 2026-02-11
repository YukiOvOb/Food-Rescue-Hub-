package com.frh.backend.controller;

import com.frh.backend.dto.CreateOrderRequest;
import com.frh.backend.dto.OrderResponseDto;
import com.frh.backend.dto.RejectOrderRequest;
import com.frh.backend.mapper.OrderResponseMapper;
import com.frh.backend.model.Order;
import com.frh.backend.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints consumed by the supplier's order-queue page and by the consumer's mobile app. */
@RestController
@CrossOrigin(origins = "*")
public class SupplierOrderController {

  @Autowired private OrderService orderService;

  @Autowired private OrderResponseMapper orderResponseMapper;

  // CONSUMER – place an order

  /**
   * POST /api/orders Body: { listingId, consumerId, quantity, pickupSlotStart?, pickupSlotEnd? }
   */
  @PostMapping("/api/consumer/orders")
  public ResponseEntity<OrderResponseDto> createOrder(
      @Valid @RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(orderResponseMapper.toOrderResponse(order));
  }

  // SUPPLIER – view order queue

  /**
   * GET /api/supplier/orders/{storeId}?status=PENDING
   *
   * <p>{@code status} is optional. Omit to get all orders for the store.
   */
  @GetMapping("/api/supplier/orders/{storeId}")
  public ResponseEntity<List<OrderSummaryDto>> getOrderQueue(
      @PathVariable Long storeId, @RequestParam(required = false) String status) {

    List<OrderSummaryDto> queue = orderService.getOrderQueue(storeId, status);
    return ResponseEntity.ok(queue);
  }

  // SUPPLIER – accept

  /** PUT /api/supplier/orders/{orderId}/accept No body required. */
  @PutMapping("/api/supplier/orders/{orderId}/accept")
  public ResponseEntity<OrderResponseDto> acceptOrder(@PathVariable Long orderId) {
    Order updated = orderService.acceptOrder(orderId);
    return ResponseEntity.ok(orderResponseMapper.toOrderResponse(updated));
  }

  // SUPPLIER – reject

  /** PUT /api/supplier/orders/{orderId}/reject Body: { "reason": "Out of stock for this item" } */
  @PutMapping("/api/supplier/orders/{orderId}/reject")
  public ResponseEntity<OrderResponseDto> rejectOrder(
      @PathVariable Long orderId, @Valid @RequestBody RejectOrderRequest body) {

    Order updated = orderService.rejectOrder(orderId, body.getReason());
    return ResponseEntity.ok(orderResponseMapper.toOrderResponse(updated));
  }

  // SUPPLIER – cancel an already-accepted order (restores stock)

  /** PUT /api/supplier/orders/{orderId}/cancel Body: { "reason": "..." } */
  @PutMapping("/api/supplier/orders/{orderId}/cancel")
  public ResponseEntity<OrderResponseDto> cancelAcceptedOrder(
      @PathVariable Long orderId, @Valid @RequestBody RejectOrderRequest body) {

    Order updated = orderService.cancelAcceptedOrder(orderId, body.getReason());
    return ResponseEntity.ok(orderResponseMapper.toOrderResponse(updated));
  }
}

// * URL layout

// * POST /api/orders – consumer places an order
// * GET /api/supplier/orders/{storeId} – supplier views queue
// * PUT /api/supplier/orders/{orderId}/accept – supplier accepts
// * PUT /api/supplier/orders/{orderId}/reject – supplier rejects
// * PUT /api/supplier/orders/{orderId}/cancel – supplier cancels (after accept)
