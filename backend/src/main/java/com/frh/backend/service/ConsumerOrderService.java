package com.frh.backend.service;

import com.frh.backend.Model.Order;
import com.frh.backend.repository.ConsumerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConsumerOrderService {

    @Autowired
    private ConsumerOrderRepository consumerOrderRepository;

    /**
     * Get all orders
     * @return list of all orders
     */
    public List<Order> getAllOrders() {
        return consumerOrderRepository.findAll();
    }

    /**
     * Get order by ID
     * @param orderId the order ID
     * @return the order if found
     */
    public Optional<Order> getOrderById(Long orderId) {
        return consumerOrderRepository.findById(orderId);
    }

    /**
     * Get all orders for a specific consumer
     * @param consumerId the consumer's ID
     * @return list of orders for the consumer
     */
    public List<Order> getOrdersByConsumerId(Long consumerId) {
        return consumerOrderRepository.findByConsumer_ConsumerIdOrderByCreatedAtDesc(consumerId);
    }

    /**
     * Get orders by status
     * @param status the order status
     * @return list of orders with the specified status
     */
    public List<Order> getOrdersByStatus(String status) {
        return consumerOrderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get orders for a specific consumer with a specific status
     * @param consumerId the consumer's ID
     * @param status the order status
     * @return list of orders
     */
    public List<Order> getOrdersByConsumerIdAndStatus(Long consumerId, String status) {
        return consumerOrderRepository.findByConsumer_ConsumerIdAndStatusOrderByCreatedAtDesc(consumerId, status);
    }

    /**
     * Create a new order
     * @param order the order to create
     * @return the created order
     */
    public Order createOrder(Order order) {
        return consumerOrderRepository.save(order);
    }

    /**
     * Update an existing order
     * @param orderId the order ID
     * @param updatedOrder the updated order data
     * @return the updated order
     */
    public Order updateOrder(Long orderId, Order updatedOrder) {
        return consumerOrderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(updatedOrder.getStatus());
                    order.setPickupSlotStart(updatedOrder.getPickupSlotStart());
                    order.setPickupSlotEnd(updatedOrder.getPickupSlotEnd());
                    order.setTotalAmount(updatedOrder.getTotalAmount());
                    order.setCurrency(updatedOrder.getCurrency());
                    order.setCancelReason(updatedOrder.getCancelReason());
                    return consumerOrderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    /**
     * Delete an order
     * @param orderId the order ID
     */
    public void deleteOrder(Long orderId) {
        consumerOrderRepository.deleteById(orderId);
    }

    /**
     * Update order status
     * @param orderId the order ID
     * @param status the new status
     * @return the updated order
     */
    public Order updateOrderStatus(Long orderId, String status) {
        return consumerOrderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    return consumerOrderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }
}
