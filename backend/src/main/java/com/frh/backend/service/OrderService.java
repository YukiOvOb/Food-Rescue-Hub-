package com.frh.backend.service;

import com.frh.backend.Model.Order;
import com.frh.backend.Model.Store;
import com.frh.backend.Model.ConsumerProfile;
import com.frh.backend.Model.PickupToken;
import com.frh.backend.repository.OrderRepository;
import com.frh.backend.repository.PickupTokenRepository;
import com.frh.backend.repository.StoreRepository;
import com.frh.backend.repository.ConsumerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final ConsumerProfileRepository consumerRepository;
    private final PickupTokenRepository pickupTokenRepository;

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Create a new order
     */
    @Transactional
    public Order createOrder(Long storeId, Long consumerId, BigDecimal totalAmount, 
                            LocalDateTime pickupSlotStart, LocalDateTime pickupSlotEnd) {
        
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));
        
        ConsumerProfile consumer = consumerRepository.findById(consumerId)
            .orElseThrow(() -> new RuntimeException("Consumer not found with id: " + consumerId));
        
        Order order = new Order();
        order.setStore(store);
        order.setConsumer(consumer);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCurrency("SGD");
        order.setPickupSlotStart(pickupSlotStart);
        order.setPickupSlotEnd(pickupSlotEnd);
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getOrderId());
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
        if ("COMPLETED".equalsIgnoreCase(status)) {
            pickupTokenRepository.findByOrderId(orderId).orElseGet(() -> {
                PickupToken token = new PickupToken();
                token.setOrder(order);
                token.setOrderId(order.getOrderId());
                token.setQrTokenHash(generateUniqueToken());
                token.setExpiresAt(LocalDateTime.now().plusDays(1));
                return pickupTokenRepository.save(token);
            });
        }
        log.info("Order status updated to {} for order ID: {}", status, orderId);
        return updatedOrder;
    }

    private String generateUniqueToken() {
        for (int i = 0; i < 10; i++) {
            String token = generateTokenValue();
            if (pickupTokenRepository.findByQrTokenHash(token).isEmpty()) {
                return token;
            }
        }
        throw new RuntimeException("Failed to generate unique pickup token");
    }

    private String generateTokenValue() {
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int idx = SECURE_RANDOM.nextInt(TOKEN_CHARS.length());
            sb.append(TOKEN_CHARS.charAt(idx));
        }
        return sb.toString();
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
