package com.frh.backend.service;

import com.frh.backend.Model.*;
import com.frh.backend.dto.CreateOrderRequest;
import com.frh.backend.dto.OrderSummaryDTO;
import com.frh.backend.exception.InsufficientStockException;
import com.frh.backend.exception.OrderStateException;
import com.frh.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * this service everything that happens around an Order lifecycle.
 *
 * for the userstory 7 – accept / reject a pending order
 * for the userstory 13 – inventory is decremented on accept (delegated to
 * InventoryService)
 */

@Service
public class OrderService {

  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private ListingRepository listingRepository;
  @Autowired
  private ConsumerProfileRepository consumerProfileRepository;
  @Autowired
  private InventoryService inventoryService;

  // CREATE – consumer places an order

  /**
   * 1. Validates listing exists and is ACTIVE.
   * 2. Performs a fast (unlocked) stock check – fails early with a friendly
   * message.
   * 3. Builds an Order in PENDING state and persists it.
   *
   * The real oversell guard fires later when the supplier clicks ACCEPT
   * (inside {@link #acceptOrder}), but checking here avoids letting
   * obviously-impossible orders into the queue at all.
   */

  @Transactional
  public Order createOrder(CreateOrderRequest req) {
    // this resolve listing
    Listing listing = listingRepository.findById(req.getListingId())
        .orElseThrow(() -> new RuntimeException("Listing not found with id: " + req.getListingId()));

    if (!"ACTIVE".equalsIgnoreCase(listing.getStatus())) {
      throw new RuntimeException("Listing " + req.getListingId() + " is not active");
    }

    // this resolves consumer
    ConsumerProfile consumer = consumerProfileRepository.findById(req.getConsumerId())
        .orElseThrow(() -> new RuntimeException("Consumer not found with id: " + req.getConsumerId()));

    // fast stock check (no lock – just an early exit)
    if (!inventoryService.checkStock(req.getListingId(), req.getQuantity())) {
      throw new InsufficientStockException(
          req.getListingId(),
          req.getQuantity(),
          inventoryService.getInventory(req.getListingId()).getQtyAvailable());
    }

    // building the order
    Order order = new Order();
    order.setStore(listing.getStore());
    order.setConsumer(consumer);
    order.setStatus("PENDING");
    order.setTotalAmount(listing.getRescuePrice().multiply(java.math.BigDecimal.valueOf(req.getQuantity())));
    order.setCurrency("SGD");
    order.setPickupSlotStart(req.getPickupSlotStart());
    order.setPickupSlotEnd(req.getPickupSlotEnd());

    Order savedOrder = orderRepository.save(order);

    // add the order-item line
    OrderItem item = new OrderItem();
    item.setOrder(savedOrder);
    item.setListing(listing);
    item.setQuantity(req.getQuantity());
    item.setUnitPrice(listing.getRescuePrice());
    // lineTotal is calculated by @PrePersist on OrderItem
    savedOrder.getOrderItems().add(item);

    return orderRepository.save(savedOrder);
  }

  // ACCEPT – supplier accepts a pending order (US 7)

  @Transactional
  public Order acceptOrder(Long orderId) {
    Order order = orderRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

    // state guard
    if (!"PENDING".equals(order.getStatus())) {
      throw new OrderStateException(orderId, order.getStatus(), "PENDING");
    }

    // decrement every line item's listing inventory
    for (OrderItem item : order.getOrderItems()) {
      inventoryService.decrementStock(item.getListing().getListingId(), item.getQuantity());
    }

    order.setStatus("ACCEPTED");
    return orderRepository.save(order);
  }

  // REJECT – supplier rejects a pending order (US 7)

  /**
   * Inventory is NOT touched here – it was never decremented for a PENDING order.
   * If we later add a "cancel an ACCEPTED order" flow, call
   * {@link InventoryService#restoreStock} inside that method.
   */
  @Transactional
  public Order rejectOrder(Long orderId, String reason) {
    Order order = orderRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

    if (!"PENDING".equals(order.getStatus())) {
      throw new OrderStateException(orderId, order.getStatus(), "PENDING");
    }

    order.setStatus("REJECTED");
    order.setCancelReason(reason);
    return orderRepository.save(order);
  }

  // CANCEL an already-ACCEPTED order (to restores the stock)

  @Transactional
  public Order cancelAcceptedOrder(Long orderId, String reason) {
    Order order = orderRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

    if (!"ACCEPTED".equals(order.getStatus())) {
      throw new OrderStateException(orderId, order.getStatus(), "ACCEPTED");
    }

    // restore inventory that was decremented on accept
    for (OrderItem item : order.getOrderItems()) {
      inventoryService.restoreStock(item.getListing().getListingId(), item.getQuantity());
    }
    order.setStatus("CANCELLED");
    order.setCancelReason(reason);
    return orderRepository.save(order);
  }

  // QUERIES

  /**
   * Returns the supplier's order queue for a specific store.
   * {@code status} is optional – pass {@code null} for all statuses.
   */
  @Transactional(readOnly = true)
  public List<OrderSummaryDTO> getOrderQueue(Long storeId, String status) {
    List<Order> orders = orderRepository.findByStoreIdAndStatus(storeId, status);
    return orders.stream()
        .map(this::toSummaryDTO)
        .collect(Collectors.toList());
  }

  // DTO mapper
  private OrderSummaryDTO toSummaryDTO(Order o) {
    // grab the first order-item for the title (one listing per order in this MVP)
    String title = "—";
    Long listingId = null;
    int qty = 0;
    java.math.BigDecimal unitPrice = java.math.BigDecimal.ZERO;

    if (o.getOrderItems() != null && !o.getOrderItems().isEmpty()) {
      OrderItem first = o.getOrderItems().get(0);
      listingId = first.getListing().getListingId();
      title = first.getListing().getTitle();
      qty = first.getQuantity();
      unitPrice = first.getUnitPrice();
    }

    return OrderSummaryDTO.builder()
        .orderId(o.getOrderId())
        .status(o.getStatus())
        .listingId(listingId)
        .listingTitle(title)
        .quantity(qty)
        .unitPrice(unitPrice)
        .totalAmount(o.getTotalAmount())
        .consumerId(o.getConsumer().getConsumerId())
        .consumerName(o.getConsumer().getDisplayName())
        .consumerPhone(o.getConsumer().getPhone())
        .pickupSlotStart(o.getPickupSlotStart())
        .pickupSlotEnd(o.getPickupSlotEnd())
        .createdAt(o.getCreatedAt())
        .cancelReason(o.getCancelReason())
        .build();
  }
}