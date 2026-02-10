package com.frh.backend.controller;

import com.frh.backend.Model.Order;
import com.frh.backend.service.OrderService;
import com.frh.backend.dto.CreateOrderResponseDto;
import com.frh.backend.dto.ErrorResponse;
import com.frh.backend.exception.InsufficientStockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private final OrderService orderService;

    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            HttpSession session,
            @RequestParam(required = false) LocalDateTime pickupSlotStart,
            @RequestParam(required = false) LocalDateTime pickupSlotEnd) {
        
        try {
            // Get consumerId from session
            Long consumerId = (Long) session.getAttribute("USER_ID");
            String user_role = (String) session.getAttribute("USER_ROLE");
            if (consumerId == null || !"CONSUMER".equals(user_role)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "User not authorised"));
            }
            
            Order order = orderService.createOrderFromCart(consumerId, 
                                                   pickupSlotStart, pickupSlotEnd);
            log.info("Order created successfully with ID: {}", order.getOrderId());
            String pickupToken = order.getPickupToken() != null ? order.getPickupToken().getQrTokenHash() : null;
            CreateOrderResponseDto response = new CreateOrderResponseDto();
            response.setOrderId(order.getOrderId());
            response.setTotalAmount(order.getTotalAmount());
            response.setPickupToken(pickupToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InsufficientStockException e) {
            throw e;
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * Get order by ID
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
            return ResponseEntity.ok(order);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error retrieving order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve order: " + e.getMessage()));
        }
    }

    /**
     * Get all orders
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error retrieving orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Get orders by SESSION id
     */
    @GetMapping("/consumer")
    public ResponseEntity<?> getOrdersByConsumer(HttpSession session) {
        Long consumerId = (Long) session.getAttribute("USER_ID");
        String user_role = (String) session.getAttribute("USER_ROLE");

        if (!"CONSUMER".equals(user_role)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Only accessible to consumers"));
        }

        if (consumerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "User not authorised"));
        }

        try {
            List<Order> orders = orderService.getOrdersByConsumer(consumerId);
            return ResponseEntity.ok(orders);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error retrieving consumer orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Get orders by store ID
     * GET /api/orders/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getOrdersByStore(@PathVariable Long storeId) {
        try {
            List<Order> orders = orderService.getOrdersByStore(storeId);
            return ResponseEntity.ok(orders);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error retrieving store orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Get orders by status
     * GET /api/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error retrieving orders by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Get orders by store and status
     * GET /api/orders/store/{storeId}/status/{status}
     */
    @GetMapping("/store/{storeId}/status/{status}")
    public ResponseEntity<?> getOrdersByStoreAndStatus(
            @PathVariable Long storeId,
            @PathVariable String status) {
        try {
            List<Order> orders = orderService.getOrdersByStoreAndStatus(storeId, status);
            return ResponseEntity.ok(orders);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error retrieving store orders by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Update order
     * PUT /api/orders/{orderId}
     */
    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long orderId,
            @RequestBody Order orderDetails) {
        
        try {
            Order updatedOrder = orderService.updateOrder(orderId, orderDetails);
            log.info("Order updated successfully with ID: {}", orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error updating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update order: " + e.getMessage()));
        }
    }

    /**
     * Update order status
     * PATCH /api/orders/{orderId}/status
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        
        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            log.info("Order status updated to {} for order ID: {}", status, orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update order status: " + e.getMessage()));
        }
    }

    /**
     * Cancel order
     * PATCH /api/orders/{orderId}/cancel
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String cancelReason) {
        
        try {
            Order cancelledOrder = orderService.cancelOrder(orderId, cancelReason);
            log.info("Order cancelled with ID: {}", orderId);
            return ResponseEntity.ok(cancelledOrder);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error cancelling order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to cancel order: " + e.getMessage()));
        }
    }   

    /**
     * Delete order
     * DELETE /api/orders/{orderId}
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            log.info("Order deleted successfully with ID: {}", orderId);
            return ResponseEntity.ok().body("Order deleted successfully");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getStatusCode().value(), e.getReason()));
        } catch (Exception e) {
            log.error("Error deleting order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete order: " + e.getMessage()));
        }
    }

    
}
