package com.frh.backend.service;

import com.frh.backend.Model.*;
import com.frh.backend.exception.InsufficientStockException;
import com.frh.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final ConsumerProfileRepository consumerRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ListingRepository listingRepository;

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }

    /**
     * Create a new order from cart
     */
    @Transactional
    public Order createOrderFromCart(Long consumerId, LocalDateTime pickupSlotStart, LocalDateTime pickupSlotEnd) {
        Cart cart = cartRepository.findFirstByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(consumerId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("No active cart found"));

        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        if (items.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        if (pickupSlotStart == null || pickupSlotEnd == null || !pickupSlotStart.isBefore(pickupSlotEnd)) {
            throw new RuntimeException("Invalid pickup slot");
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
        
        // Save order first to generate orderId
        Order savedOrder = orderRepository.save(order);

        // Generate pickup token
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        String qrTokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        PickupToken pickupToken = new PickupToken();
        pickupToken.setOrder(savedOrder); // This sets the orderId via @MapsId
        pickupToken.setQrTokenHash(qrTokenHash);
        pickupToken.setExpiresAt(expiresAt);
        
        savedOrder.setPickupToken(pickupToken);

        // Clear cart
        cartItemRepository.deleteByCart_CartId(cart.getCartId());
        cart.setStore(null);
        cartRepository.save(cart);

        log.info("Order created from cart with ID: {} and pickup token generated", savedOrder.getOrderId());
        return savedOrder;
    }
    
    /**
     * Get order by ID
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Get all orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Get orders by consumer ID
     */
    public List<Order> getOrdersByConsumer(Long consumerId) {
        return orderRepository.findByConsumer_ConsumerId(consumerId);
    }

    /**
     * Get orders by store ID
     */
    public List<Order> getOrdersByStore(Long storeId) {
        return orderRepository.findByStore_StoreId(storeId);
    }

    /**
     * Get orders by status
     */
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Get orders by store and status
     */
    public List<Order> getOrdersByStoreAndStatus(Long storeId, String status) {
        return orderRepository.findByStore_StoreIdAndStatus(storeId, status);
    }

    /**
     * Update order
     */
    @Transactional
    public Order updateOrder(Long orderId, Order orderDetails) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
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
     * Update order status
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated to {} for order ID: {}", status, orderId);
        return updatedOrder;
    }

    /**
     * Delete order
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
        log.info("Order deleted successfully with ID: {}", orderId);
    }

    /**
     * Cancel order
     */
    @Transactional
    public Order cancelOrder(Long orderId, String cancelReason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        order.setStatus("CANCELLED");
        order.setCancelReason(cancelReason);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order cancelled with ID: {}", orderId);
        return updatedOrder;
    }

    /**
     * Count orders by status
     */
    public long countOrdersByStatus(String status) {
        return getOrdersByStatus(status).size();
    }

    /**
     * Count pending orders for a store
     */
    public long countPendingOrdersForStore(Long storeId) {
        return getOrdersByStoreAndStatus(storeId, "PENDING").size();
    }
}
