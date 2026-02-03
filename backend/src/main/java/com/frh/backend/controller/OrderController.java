package com.frh.backend.controller;

import com.frh.backend.Model.Order;
import com.frh.backend.service.OrderService;
import com.frh.backend.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {


    /**
     * 下面是验证各接口的curl（把172.26.235.205替换成你的WSL IP即可）：

创建订单（CREATE）
curl -X POST "http://172.26.235.205:8080/api/orders?storeId=1&consumerId=1&totalAmount=12.50&pickupSlotStart=2026-01-31T10:00:00&pickupSlotEnd=2026-01-31T11:00:00"

查询所有订单（READ ALL）
curl "http://172.26.235.205:8080/api/orders"

按ID查询（READ BY ID）
curl "http://172.26.235.205:8080/api/orders/1"

按消费者查询
curl "http://172.26.235.205:8080/api/orders/consumer/1"

按店铺查询
curl "http://172.26.235.205:8080/api/orders/store/1"

按状态查询
curl "http://172.26.235.205:8080/api/orders/status/PENDING"

按店铺+状态查询
curl "http://172.26.235.205:8080/api/orders/store/1/status/PENDING"

更新订单（UPDATE）
curl -X PUT "http://172.26.235.205:8080/api/orders/1"
-H "Content-Type: application/json"
-d '{"status":"CONFIRMED","totalAmount":15.00,"cancelReason":null}'

更新订单状态
curl -X PATCH "http://172.26.235.205:8080/api/orders/1/status?status=COMPLETED"

取消订单
curl -X PATCH "http://172.26.235.205:8080/api/orders/1/cancel?cancelReason=Customer%20requested"

删除订单（DELETE）
curl -X DELETE "http://172.26.235.205:8080/api/orders/1"
     */

    private final OrderService orderService;

    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestParam Long storeId,
            @RequestParam Long consumerId,
            @RequestParam BigDecimal totalAmount,
            @RequestParam(required = false) LocalDateTime pickupSlotStart,
            @RequestParam(required = false) LocalDateTime pickupSlotEnd) {
        
        try {
            Order order = orderService.createOrder(storeId, consumerId, totalAmount, 
                                                   pickupSlotStart, pickupSlotEnd);
            log.info("Order created successfully with ID: {}", order.getOrderId());
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Failed to create order"));
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
                .orElseThrow(() -> new RuntimeException("Order not found"));
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error retrieving order", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Order not found"));
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
        } catch (Exception e) {
            log.error("Error retrieving orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders"));
        }
    }

    /**
     * Get orders by consumer ID
     * GET /api/orders/consumer/{consumerId}
     */
    @GetMapping("/consumer/{consumerId}")
    public ResponseEntity<?> getOrdersByConsumer(@PathVariable Long consumerId) {
        try {
            List<Order> orders = orderService.getOrdersByConsumer(consumerId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error retrieving consumer orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders"));
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
        } catch (Exception e) {
            log.error("Error retrieving store orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders"));
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
        } catch (Exception e) {
            log.error("Error retrieving orders by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders"));
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
        } catch (Exception e) {
            log.error("Error retrieving store orders by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve orders"));
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
        } catch (Exception e) {
            log.error("Error updating order", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Failed to update order"));
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
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Failed to update order status"));
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
        } catch (Exception e) {
            log.error("Error cancelling order", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Failed to cancel order"));
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
        } catch (Exception e) {
            log.error("Error deleting order", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Failed to delete order"));
        }
    }
}
