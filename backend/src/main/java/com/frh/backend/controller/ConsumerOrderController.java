package com.frh.backend.controller;

import com.frh.backend.Model.Order;
import com.frh.backend.dto.OrderResponseDto;
import com.frh.backend.mapper.OrderResponseMapper;
import com.frh.backend.service.ConsumerOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumer/orders")
@CrossOrigin(origins = "*")
public class ConsumerOrderController {

    @Autowired
    private ConsumerOrderService consumerOrderService;

    @Autowired
    private OrderResponseMapper orderResponseMapper;

    /**
     * Get all orders for a specific consumer
     * @return list of orders for the consumer
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrdersByConsumerId(HttpSession session) {
        Long consumerId = getSessionConsumerId(session);
        if (consumerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Order> orders = consumerOrderService.getOrdersByConsumerId(consumerId);
        return ResponseEntity.ok(orderResponseMapper.toOrderResponseList(orders));
    }

    /**
     * Get order by ID
     * @param orderId the order ID
     * @return the order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            HttpSession session,
            @PathVariable Long orderId) {
        Long consumerId = getSessionConsumerId(session);
        if (consumerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Order order = consumerOrderService.getOrderById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isOrderOwnedByConsumer(order, consumerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orderResponseMapper.toOrderResponse(order));
    }

    /**
     * Get orders for a specific consumer with a specific status
     * @param status the order status
     * @return list of orders
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByConsumerIdAndStatus(
            HttpSession session,
            @PathVariable String status) {
        Long consumerId = getSessionConsumerId(session);
        if (consumerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Order> orders = consumerOrderService.getOrdersByConsumerIdAndStatus(consumerId, status);
        return ResponseEntity.ok(orderResponseMapper.toOrderResponseList(orders));
    }

    /**
     * Update order status
     * @param orderId the order ID
     * @param status the new status
     * @return the updated order
     */
    @PatchMapping("/order/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            HttpSession session,
            @PathVariable Long orderId,
            @RequestParam String status) {
        Long consumerId = getSessionConsumerId(session);
        if (consumerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Order existingOrder = consumerOrderService.getOrderById(orderId)
                    .orElse(null);
            if (existingOrder == null) {
                return ResponseEntity.notFound().build();
            }

            if (!isOrderOwnedByConsumer(existingOrder, consumerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Order updatedOrder = consumerOrderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(orderResponseMapper.toOrderResponse(updatedOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Long getSessionConsumerId(HttpSession session) {
        return (Long) session.getAttribute("USER_ID");
    }

    private boolean isOrderOwnedByConsumer(Order order, Long consumerId) {
        return order.getConsumer() != null
                && order.getConsumer().getConsumerId() != null
                && order.getConsumer().getConsumerId().equals(consumerId);
    }
}
