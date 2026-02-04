package com.frh.backend.controller;

import com.frh.backend.Model.Order;
import com.frh.backend.service.ConsumerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumer/orders")
@CrossOrigin(origins = "*")
public class ConsumerOrderController {

    @Autowired
    private ConsumerOrderService consumerOrderService;

    /**
     * Get all orders for a specific consumer
     * @param consumerId the consumer's ID
     * @return list of orders for the consumer
     */
    @GetMapping("/{consumerId}")
    public ResponseEntity<List<Order>> getOrdersByConsumerId(@PathVariable Long consumerId) {
        List<Order> orders = consumerOrderService.getOrdersByConsumerId(consumerId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order by ID
     * @param consumerId the consumer's ID
     * @param orderId the order ID
     * @return the order
     */
    @GetMapping("/{consumerId}/order/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long consumerId,
            @PathVariable Long orderId) {
        return consumerOrderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get orders for a specific consumer with a specific status
     * @param consumerId the consumer's ID
     * @param status the order status
     * @return list of orders
     */
    @GetMapping("/{consumerId}/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByConsumerIdAndStatus(
            @PathVariable Long consumerId,
            @PathVariable String status) {
        List<Order> orders = consumerOrderService.getOrdersByConsumerIdAndStatus(consumerId, status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Update order status
     * @param consumerId the consumer's ID
     * @param orderId the order ID
     * @param status the new status
     * @return the updated order
     */
    @PatchMapping("/{consumerId}/order/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long consumerId,
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            Order updatedOrder = consumerOrderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
