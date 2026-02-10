package com.frh.backend.service;

import com.frh.backend.Model.*;
import com.frh.backend.dto.CreateOrderRequest;
import com.frh.backend.dto.OrderSummaryDTO;
import com.frh.backend.exception.InsufficientStockException;
import com.frh.backend.exception.OrderStateException;
import com.frh.backend.repository.*;
import com.frh.backend.util.PickupTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

/**
 * This service handles everything that happens around an Order lifecycle.
 *
 * For user story 7 – accept/reject a pending order.
 * For user story 13 – inventory is decremented on accept (delegated to
 * {@link InventoryService}).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final ConsumerProfileRepository consumerProfileRepository;
    private final InventoryService inventoryService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Create a new order from cart.
     */
    @Transactional
    public Order createOrderFromCart(Long consumerId, LocalDateTime pickupSlotStart, LocalDateTime pickupSlotEnd) {
        Cart cart = cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(consumerId, "ACTIVE")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active cart found"));

        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        if (pickupSlotStart == null || pickupSlotEnd == null || !pickupSlotStart.isBefore(pickupSlotEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pickup slot");
        }

        Order order = new Order();
        order.setConsumer(cart.getConsumer());
        order.setStore(cart.getStore());
        order.setStatus("PENDING");
        order.setPickupSlotStart(pickupSlotStart);
        order.setPickupSlotEnd(pickupSlotEnd);

        // Sort by listingId to reduce deadlock risk
        List<CartItem> sortedItems = items.stream()
            .sorted(Comparator.comparing(i -> i.getListing().getListingId()))
            .collect(Collectors.toList());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : sortedItems) {
            Long listingId = item.getListing().getListingId();
            Listing listing = listingRepository.findByIdForUpdate(listingId);
            if (listing == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
            }

            int availableQty = listing.getAvailableQty();
            if (availableQty < item.getQuantity()) {
                throw new InsufficientStockException(listingId, item.getQuantity(), availableQty);
            }

            listing.setAvailableQty(availableQty - item.getQuantity());
            listingRepository.save(listing);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setListing(listing);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(listing.getRescuePrice());
            orderItem.setLineTotal(listing.getRescuePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            order.getOrderItems().add(orderItem);

            totalAmount = totalAmount.add(orderItem.getLineTotal());
        }

        order.setTotalAmount(totalAmount);
        order.setPickupToken(PickupTokenGenerator.createForOrder(order));

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteByCart_CartId(cart.getCartId());
        cart.setStore(null);
        cartRepository.save(cart);

        log.info("Order created from cart with ID: {} and pickup token generated", savedOrder.getOrderId());
        return savedOrder;
    }

    // CREATE – consumer places an order

    /**
     * 1. Validates listing exists and is ACTIVE.
     * 2. Performs a fast (unlocked) stock check – fails early with a friendly message.
     * 3. Builds an Order in PENDING state and persists it.
     *
     * The real oversell guard fires later when the supplier clicks ACCEPT
     * (inside {@link #acceptOrder}), but checking here avoids letting
     * obviously-impossible orders into the queue at all.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        Listing listing = listingRepository.findById(req.getListingId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found with id: " + req.getListingId()));

        if (!"ACTIVE".equalsIgnoreCase(listing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Listing " + req.getListingId() + " is not active");
        }

        ConsumerProfile consumer = consumerProfileRepository.findById(req.getConsumerId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consumer not found with id: " + req.getConsumerId()));

        // fast stock check (no lock – just an early exit)
        if (!inventoryService.checkStock(req.getListingId(), req.getQuantity())) {
            throw new InsufficientStockException(
                req.getListingId(),
                req.getQuantity(),
                inventoryService.getInventory(req.getListingId()).getQtyAvailable());
        }

        Order order = new Order();
        order.setStore(listing.getStore());
        order.setConsumer(consumer);
        order.setStatus("PENDING");
        order.setTotalAmount(listing.getRescuePrice().multiply(BigDecimal.valueOf(req.getQuantity())));
        order.setCurrency("SGD");
        order.setPickupSlotStart(req.getPickupSlotStart());
        order.setPickupSlotEnd(req.getPickupSlotEnd());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setListing(listing);
        item.setQuantity(req.getQuantity());
        item.setUnitPrice(listing.getRescuePrice());
        // lineTotal is calculated by @PrePersist on OrderItem
        order.getOrderItems().add(item);
        order.setPickupToken(PickupTokenGenerator.createForOrder(order));

        return orderRepository.save(order);
    }

    // ACCEPT – supplier accepts a pending order (US 7)
    @Transactional
    public Order acceptOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));

        if (!"PENDING".equals(order.getStatus())) {
            throw new OrderStateException(orderId, order.getStatus(), "PENDING");
        }

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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));

        if (!"PENDING".equals(order.getStatus())) {
            throw new OrderStateException(orderId, order.getStatus(), "PENDING");
        }

        order.setStatus("REJECTED");
        order.setCancelReason(reason);
        return orderRepository.save(order);
    }

    // CANCEL an already-ACCEPTED order (to restore the stock)
    @Transactional
    public Order cancelAcceptedOrder(Long orderId, String reason) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));

        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new OrderStateException(orderId, order.getStatus(), "ACCEPTED");
        }

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

    private OrderSummaryDTO toSummaryDTO(Order o) {
        String title = "—";
        Long listingId = null;
        int qty = 0;
        BigDecimal unitPrice = BigDecimal.ZERO;

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

    /**
     * Get order by ID.
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Get all orders.
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Get orders by consumer ID.
     */
    public List<Order> getOrdersByConsumer(Long consumerId) {
        return orderRepository.findByConsumer_ConsumerId(consumerId);
    }

    /**
     * Get orders by store ID.
     */
    public List<Order> getOrdersByStore(Long storeId) {
        return orderRepository.findByStore_StoreId(storeId);
    }

    /**
     * Get orders by status.
     */
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Get orders by store and status.
     */
    public List<Order> getOrdersByStoreAndStatus(Long storeId, String status) {
        return orderRepository.findByStore_StoreIdAndStatus(storeId, status);
    }

    /**
     * Update order.
     */
    @Transactional
    public Order updateOrder(Long orderId, Order orderDetails) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));

        if (orderDetails.getStatus() != null) {
            order.setStatus(orderDetails.getStatus());
        }

        if (orderDetails.getTotalAmount() != null) {
            order.setTotalAmount(orderDetails.getTotalAmount());
        }

        if (orderDetails.getPickupSlotStart() != null) {
            order.setPickupSlotStart(orderDetails.getPickupSlotStart());
        }

        if (orderDetails.getPickupSlotEnd() != null) {
            order.setPickupSlotEnd(orderDetails.getPickupSlotEnd());
        }

        if (orderDetails.getCancelReason() != null) {
            order.setCancelReason(orderDetails.getCancelReason());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated successfully with ID: {}", orderId);
        return updatedOrder;
    }

    /**
     * Update order status.
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated to {} for order ID: {}", status, orderId);
        return updatedOrder;
    }

    /**
     * Delete order.
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
        log.info("Order deleted successfully with ID: {}", orderId);
    }

    /**
     * Cancel order.
     */
    @Transactional
    public Order cancelOrder(Long orderId, String cancelReason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));

        order.setStatus("CANCELLED");
        order.setCancelReason(cancelReason);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order cancelled with ID: {}", orderId);
        return updatedOrder;
    }

    /**
     * Count orders by status.
     */
    public long countOrdersByStatus(String status) {
        return getOrdersByStatus(status).size();
    }

    /**
     * Count pending orders for a store.
     */
    public long countPendingOrdersForStore(Long storeId) {
        return getOrdersByStoreAndStatus(storeId, "PENDING").size();
    }

    /**
     * Return top selling items for a supplier and status limited by `limit`.
     */
    public List<com.frh.backend.dto.TopSellingItemDto> getTopSellingItems(Long supplierId, String status, int limit) {
        return orderRepository.findTopSellingItemsBySupplierAndStatus(supplierId, status, PageRequest.of(0, Math.max(1, limit)));
    }
}
